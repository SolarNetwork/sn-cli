/**
 * 
 */
package s10k.tool.datum.imp.cmd;

import static java.nio.charset.StandardCharsets.UTF_8;
import static net.solarnetwork.codec.JsonUtils.getTreeFromObject;
import static s10k.tool.common.domain.ServiceConfiguration.SERVICE_PROPERTIES_KEY;
import static s10k.tool.common.util.RestUtils.checkSuccess;
import static s10k.tool.common.util.StringUtils.stringOrFileContents;
import static s10k.tool.datum.imp.util.DatumImportUtils.importBatchSize;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.jspecify.annotations.Nullable;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

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
import s10k.tool.datum.imp.domain.DatumImportState;
import s10k.tool.datum.imp.domain.DatumImportTaskInfo;
import s10k.tool.datum.imp.domain.DatumInputServiceConfiguration;
import s10k.tool.datum.imp.util.DatumImportRestUtils;
import s10k.tool.datum.imp.util.DatumImportUtils;

/**
 * Submit a datum import job.
 */
@Command(name = "submit", sortSynopsis = false, showDefaultValues = true, descriptionHeading = "%n", optionListHeading = "%n", description = {
		"Submit a datum import job.%n",

		"The @|bold --data-file|@ option is required by some, but not all, input services.",
		"For example the Cloud Integration service does @|bold not|@ require this, and so",
		"this option does not need to be provided.%n",

		"The job can be staged with the @|bold --stage|@ option, in which case the job will start",
		"in the @|bold Staged|@ state and not progess any further until confirmed (see the",
		"@|bold confirm-staged|@ command to do that). A staged job can also be previewed so you can",
		"verify the configuration (see the @|bold preview-staged|@ command to do that).%n",

		"If the job is @|bold not|@ staged, it will start in the @|bold Queued|@ state and will be",
		"processed by SolarNetwork at some point in the future.%n",

		"Use the @|bold view|@ or @|bold list|@ commands to monitor a job's overall status.%n" })
public class ImportDatumCmd extends BaseSubCmd<DatumImportsCmd> implements Callable<Integer> {

	// @formatter:off
	@Option(names = {"-f", "--data-file"},
			description = "the data to import",
			required = false)
	Path dataFile;
	
	@Option(names = { "-s", "--stage" },
			description = "the import job name to set")
	boolean stage;
	
	@Option(names = { "-m", "--name" },
			description = "the import job name to set")
	String name;

	@Option(names = { "-b", "--batch-size" },
			description = "the import batch size to set",
			defaultValue = "10000")
	Integer batchSize;

	@Option(names = { "-G", "--group-key" },
			description = "the group key to set")
	String groupKey;

	@Option(names = { "-tz", "--time-zone" },
			description = "a time zone to interpret the dates in the import data as")
	ZoneId zone;
	
	@Option(names = { "-S", "--service" },
			description = "the input service identifier to use; a substring of the service type can be used",
			required = true)
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
	
	@Parameters(index = "0", paramLabel = "<config>", description = "the configuration to submit, or @file for file to load", arity = "0..1")
	String value;
	// @formatter:on

	/**
	 * Constructor.
	 * 
	 * @param reqFactory   the HTTP request factory to use
	 * @param objectMapper the mapper to use
	 */
	public ImportDatumCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		final RestClient restClient = restClient();
		try {
			final Map<String, Object> settings = new LinkedHashMap<>(4);

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

			final DatumImportConfiguration importConfig = createConfiguration(settings);

			final DatumImportTaskInfo result;

			if (isDryRun()) {
				var ts = Instant.now().truncatedTo(ChronoUnit.SECONDS);
				result = new DatumImportTaskInfo(0L, "dry-run",
						(stage ? DatumImportState.Staged : DatumImportState.Queued), ts, groupKey, false, ts, null,
						null, 0, 0, importConfig);
			} else {
				final String jobId = submitDatumImportTask(restClient, objectMapper, importConfig, dataFileResource());
				result = DatumImportRestUtils.viewDatumImportTask(restClient, objectMapper, jobId);
			}

			List<?> tableData = (displayMode == ResultDisplayMode.JSON ? List.of(result)
					: List.of((Object) ViewImportJobCmd.tableDataRow(result)));
			TableUtils.renderTableData(ViewImportJobCmd.tableDataColumns(), tableData, displayMode, objectMapper,
					TableUtils.TableDataJsonPrettyPrinter.INSTANCE, System.out);
			return 0;
		} catch (Exception e) {
			System.err.println("Error confirming staged datum import job: %s".formatted(e.getMessage()));
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

	private Resource dataFileResource() {
		if (dataFile != null) {
			return new FileSystemResource(dataFile);
		}
		return new ByteArrayResource(new byte[0]) {

			@Override
			public String getFilename() {
				return "empty-file.txt";
			}
		};
	}

	private void populateSettings(Map<String, Object> settings) {
		settings.put("name", dataFileResource().getFilename());
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

	private DatumImportConfiguration createConfiguration(Map<String, Object> settings) {
		try {
			final DatumInputServiceConfiguration inputConfig = objectMapper.treeToValue(getTreeFromObject(settings),
					DatumInputServiceConfiguration.class);
			// @formatter:off
			return new DatumImportConfiguration(
					name,
					stage,
					importBatchSize(inputConfig.getServiceIdentifier(), batchSize),
					groupKey,
					inputConfig
				);
			// @formatter:on
		} catch (IOException e) {
			throw new IllegalStateException("Error generating import configuration: " + e.getMessage(), e);
		}
	}

	/**
	 * Update a datum import task.
	 * 
	 * @param restClient    the REST client
	 * @param objectMapper  the object mapper
	 * @param configuration the configuration to save
	 * @param data          the data to import
	 * @return the submitted job ID
	 * @throws IllegalStateException if an error occurs
	 */
	public static String submitDatumImportTask(RestClient restClient, ObjectMapper objectMapper,
			DatumImportConfiguration configuration, Resource data) {

		if (!data.isReadable()) {
			throw new IllegalStateException("Cannot read data file [%s]".formatted(data));
		}

		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
		body.add("config", configuration);
		body.add("data", data);

		// @formatter:off
		final JsonNode response = checkSuccess(restClient.post()
				.uri("/solaruser/api/v1/sec/user/import/jobs")
				.contentType(MediaType.MULTIPART_FORM_DATA)
				.body(body)
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.body(JsonNode.class)
			);		
			// @formatter:on

		String jobId = response.path("data").path("jobId").textValue();
		if (jobId != null) {
			return jobId;
		}
		throw new IllegalStateException("Job ID not found in datum import response: " + response);
	}

}
