package s10k.tool.datum.imp.cmd;

import static java.nio.charset.StandardCharsets.UTF_8;
import static net.solarnetwork.codec.JsonUtils.getTreeFromObject;
import static s10k.tool.common.domain.ServiceConfiguration.SERVICE_PROPERTIES_KEY;
import static s10k.tool.common.util.RestUtils.checkSuccess;
import static s10k.tool.common.util.StringUtils.stringOrFileContents;
import static s10k.tool.datum.imp.util.DatumImportRestUtils.viewDatumImportTask;
import static s10k.tool.datum.imp.util.DatumImportUtils.importBatchSize;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.jspecify.annotations.Nullable;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.solarnetwork.codec.JsonUtils;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.MergeMode;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.util.CollectionUtils;
import s10k.tool.common.util.SystemUtils;
import s10k.tool.common.util.TableUtils;
import s10k.tool.datum.imp.domain.DatumImportConfiguration;
import s10k.tool.datum.imp.domain.DatumImportTaskInfo;
import s10k.tool.datum.imp.domain.DatumInputServiceConfiguration;
import s10k.tool.datum.imp.util.DatumImportUtils;

/**
 * Update datum import job configuration.
 */
@Command(name = "update", sortSynopsis = false, descriptionHeading = "%n", optionListHeading = "%n", description = {
		"Update the input configuration of a @|bold staged|@ datum import job.%n" })
public class UpdateImportJobCmd extends BaseSubCmd<DatumImportsCmd> implements Callable<Integer> {

	// @formatter:off
	@Option(names = { "-j", "--job-id" },
			description = "the ID of the job to update",
			required = true)
	String jobId;
	
	@Option(names = { "-m", "--name" },
			description = "the import job name to set")
	String name;

	@Option(names = { "-b", "--batch-size" },
			description = "the import batch size to set")
	Integer batchSize;

	@Option(names = { "-G", "--group-key" },
			description = "the group key to set")
	String groupKey;

	@Option(names = { "-tz", "--time-zone" },
			description = "a time zone to interpret the dates in the import data as")
	ZoneId zone;
	
	@Option(names = { "-S", "--service" },
			description = "the input service identifier to set; a substring of the service type can be used")
	String serviceIdentifier;

	@Option(names = { "-g", "--merge-mode" },
			description = "the merge style to perform",
			defaultValue = "RecursiveObjects")
	MergeMode mode;

	@Option(names = { "-prop", "--service-property" },
			description = "a service property, in the form path:value",
			paramLabel = "serviceProperty")
	String serviceProperties[];

	@Option(names = {"-I", "--ignore-input"},
			description = "do not try to read settings from standard input")
	public boolean ignoreStdIn;
	
	@Option(names = { "-mode", "--display-mode" },
			description = "how to display the data",
			defaultValue = "PRETTY")
	ResultDisplayMode displayMode = ResultDisplayMode.PRETTY;
	
	@Parameters(index = "0", paramLabel = "<config>", description = "the configuration to save, or @file for file to load", arity = "0..1")
	String value;
	// @formatter:on

	/**
	 * Constructor.
	 * 
	 * @param reqFactory   the HTTP request factory to use
	 * @param objectMapper the mapper to use
	 */
	public UpdateImportJobCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		final RestClient restClient = restClient();
		try {
			final DatumImportTaskInfo existing = viewDatumImportTask(restClient, objectMapper, jobId);

			final Map<String, Object> settings = (existing != null
					? existing.configuration().inputConfiguration().toSettings()
					: new LinkedHashMap<>(4));

			// look for JSON on stdin if allowed
			if (!(ignoreStdIn || SystemUtils.systemConsoleIsTerminal())) {
				Map<String, Object> inputProps = objectMapper.readValue(new InputStreamReader(System.in, UTF_8),
						JsonUtils.STRING_MAP_TYPE);
				mergeServiceProperties(inputProps, settings);
			}

			try {
				populateSettings(settings);
			} catch (RuntimeException e) {
				System.err.println(e.getMessage());
				return 1;
			}

			if (value != null && !value.isBlank()) {
				Map<String, Object> inputProps = objectMapper.readValue(stringOrFileContents(value),
						JsonUtils.STRING_MAP_TYPE);
				mergeServiceProperties(inputProps, settings);
			}

			final DatumImportConfiguration newConfig = createConfiguration(existing, settings);

			final DatumImportTaskInfo result;

			if (isDryRun()) {
				result = existing.copyWithConfiguration(newConfig);
			} else {
				result = updateDatumImportTask(restClient, objectMapper, jobId, newConfig);
			}

			List<?> tableData = (displayMode == ResultDisplayMode.JSON ? List.of(result)
					: List.of((Object) ViewImportJobCmd.tableDataRow(result)));
			TableUtils.renderTableData(ViewImportJobCmd.tableDataColumns(), tableData, displayMode, objectMapper,
					TableUtils.TableDataJsonPrettyPrinter.INSTANCE, System.out);
			return 0;
		} catch (Exception e) {
			System.err.println("Error listing datum: %s".formatted(e.getMessage()));
		}
		return 1;
	}

	private void mergeServiceProperties(@Nullable Map<String, Object> inputProps, Map<String, Object> settings) {
		if (inputProps != null && !inputProps.isEmpty()) {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			final Map<String, Object> sprops = (Map) settings.compute(SERVICE_PROPERTIES_KEY,
					(_, v) -> v instanceof Map<?, ?> t ? (Map) t : new LinkedHashMap<>(8));
			CollectionUtils.mergeServiceProperties(inputProps, sprops, mode);
		}
	}

	private void populateSettings(Map<String, Object> settings) {
		if (name != null) {
			settings.put("name", name);
		}
		if (serviceIdentifier != null) {
			String type = DatumImportUtils.findDatumImportServiceId(serviceIdentifier).getKey();
			settings.put("serviceIdentifier", type);
		}
		if (zone != null) {
			settings.put("timeZoneId", zone.getId());
		}
		if (serviceProperties != null) {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			final Map<String, Object> sprops = (Map) settings.compute(SERVICE_PROPERTIES_KEY,
					(_, v) -> v instanceof Map<?, ?> t ? (Map) t : new LinkedHashMap<>(8));

			CollectionUtils.populateServiceProperties(serviceProperties, sprops, objectMapper);
		}
	}

	private DatumImportConfiguration createConfiguration(DatumImportTaskInfo existing, Map<String, Object> settings) {
		try {
			final DatumInputServiceConfiguration inputConfig = objectMapper.treeToValue(getTreeFromObject(settings),
					DatumInputServiceConfiguration.class);
			// @formatter:off
			return new DatumImportConfiguration(
					name != null ? name : existing.configuration().name(),
					existing.configuration().stage(),
					importBatchSize(inputConfig.getServiceIdentifier(), batchSize != null 
						? batchSize
						: existing.configuration().batchSize()),
					groupKey != null ? groupKey : existing.configuration().groupKey(),
					inputConfig
				);
			// @formatter:on
		} catch (IOException e) {
			throw new IllegalStateException("Error generating update configuration: " + e.getMessage(), e);
		}
	}

	/**
	 * Update a datum import task.
	 * 
	 * @param restClient    the REST client
	 * @param objectMapper  the object mapper
	 * @param jobId         the staged job ID to preview
	 * @param configuration the configuration to save
	 * @return the updated job info
	 * @throws IllegalStateException if an error occurs
	 */
	public static DatumImportTaskInfo updateDatumImportTask(RestClient restClient, ObjectMapper objectMapper,
			String jobId, DatumImportConfiguration configuration) {
		// @formatter:off
		final JsonNode response = checkSuccess(restClient.post()
				.uri("/solaruser/api/v1/sec/user/import/jobs/{jobId}", jobId)
				.contentType(MediaType.APPLICATION_JSON)
				.body(configuration)
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.body(JsonNode.class)
			);		
			// @formatter:on

		try {
			return objectMapper.treeToValue(response.path("data"), DatumImportTaskInfo.class);
		} catch (JsonProcessingException | IllegalArgumentException e) {
			throw new IllegalStateException("Error parsing retract datum import response: " + e.getMessage(), e);
		}
	}
}
