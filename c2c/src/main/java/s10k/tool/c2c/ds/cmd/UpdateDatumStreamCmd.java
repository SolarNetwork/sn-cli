package s10k.tool.c2c.ds.cmd;

import static java.nio.charset.StandardCharsets.UTF_8;
import static s10k.tool.c2c.util.CloudIntegrationRestUtils.viewCloudDatumStream;
import static s10k.tool.common.domain.ServiceConfiguration.SERVICE_PROPERTIES_KEY;
import static s10k.tool.common.util.RestUtils.checkSuccess;
import static s10k.tool.common.util.StringUtils.stringOrFileContents;

import java.io.InputStreamReader;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import net.solarnetwork.codec.JsonUtils;
import net.solarnetwork.domain.datum.ObjectDatumKind;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import s10k.tool.c2c.domain.CloudDatumStreamConfiguration;
import s10k.tool.c2c.util.CloudIntegrationsUtils;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.MergeMode;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.util.CollectionUtils;
import s10k.tool.common.util.SystemUtils;
import s10k.tool.common.util.TableUtils;

/**
 * Update cloud datum stream entity.
 */
@Component
@Command(name = "update", sortSynopsis = false, showDefaultValues = true, descriptionHeading = "%n", optionListHeading = "%n", description = {
		"Update a cloud datum stream. The various optional options can be used to update",
		"specific settings of a datum stream, leaving all other settings of the stream", "unchagned.%n",

		"Alternatively the settings can be provided as JSON via standard input or via",
		"an @file.json parameter. The JSON must be structured as an object as specified",
		"in the @|bold Cloud Datum Stream update|@ API in SolarNetwork. The given settings",
		"will be merged into the existing settings unless the @|bold --replace|@ option is given",
		"in which case the given JSON will completely replace the existing settings.%n", })
public class UpdateDatumStreamCmd extends BaseSubCmd<DatumStreamsCmd> implements Callable<Integer> {

	// @formatter:off
	@Option(names = { "-stream", "--stream-id" },
			description = "the datum stream ID to update",
			required = true)
	Long datumStreamId;

	@Option(names = { "-g", "--merge-mode" },
			description = "the merge style to perform",
			defaultValue = "RecursiveObjects")
	MergeMode mode;

	@Option(names = { "-S", "--service" },
			description = "the service identifier to set; a substring of the service type can be used")
	String serviceIdentifier;

	@Option(names = { "-m", "--name" },
			description = "a name to set")
	String name;

	@ArgGroup(exclusive = true, multiplicity = "0..1")
	NodeOrLocationId nodeOrLocationId;

	@Option(names = { "-source", "--source-id" },
			description = "the source ID to set")
	String sourceId;
	
	@ArgGroup(exclusive = true, multiplicity = "0..1")
	EnabledOrDisabled enabledOrDisabled;

	@Option(names = { "-map", "--mapping-id" },
			description = "the datum  stream mapping ID to set")
	Long mappingId;

	@Option(names = { "-w", "--schedule" },
			description = "the schedule to set; should be a cron schedule or a number of seconds")
	String schedule;
	
	@Option(names = { "-prop", "--service-property" },
			description = "a service property, in the form path:value",
			paramLabel = "serviceProperty")
	String serviceProperties[];

	@Option(names = {"-r", "--replace"},
			description = "when JSON input is provided, replace the settings instead of merging the given settings")
	public boolean replace;
	
	@Option(names = {"-I", "--ignore-input"},
			description = "do not try to read settings from standard input")
	public boolean ignoreStdIn;
	
	@Option(names = { "-mode", "--display-mode" },
			description = "how to display the data",
			defaultValue = "PRETTY")
	ResultDisplayMode displayMode = ResultDisplayMode.PRETTY;

	@Parameters(index = "0", paramLabel = "<config>", description = "the updates to save, or @file for file to load", arity = "0..1")
	String value;
	// @formatter:on

	/**
	 * Grouping of enabled/disabled mode flags.
	 */
	static class EnabledOrDisabled {

		// @formatter:off
		@Option(names = {"-e", "--enabled"},
				description = "make enabled")
		public boolean enabled;
		
		@Option(names = {"-d", "--disabled"},
				description = "make disabled")
		public boolean disabled;
		// @formatter:on

		/**
		 * Test if enabled or disabled.
		 * 
		 * @return {@code true} if {@code enabled}
		 */
		boolean isEnabled() {
			return enabled;
		}

	}

	/**
	 * Grouping of node/location ID.
	 */
	static class NodeOrLocationId {
		// @formatter:off
    	@Option(names = { "-node", "--node-id" },
    			description = "the node ID to set")
    	Long nodeId;

    	@Option(names = { "-loc", "--location-id" },
    			description = "the location ID to set")
    	Long locationId;
    	// @formatter:on

		/**
		 * Test if location ID is provided (otherwise node ID is).
		 * 
		 * @return {@code true} if location ID is configured
		 */
		boolean isLocation() {
			return locationId != null;
		}

	}

	/**
	 * Constructor.
	 * 
	 * @param reqFactory   the HTTP request factory to use
	 * @param objectMapper the mapper to use
	 */
	public UpdateDatumStreamCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		final RestClient restClient = restClient();
		final ObjectWriter pretty = objectMapper.writerWithDefaultPrettyPrinter();
		try {
			CloudDatumStreamConfiguration existing = viewCloudDatumStream(restClient, objectMapper, datumStreamId);

			final Map<String, Object> settings = (replace ? new LinkedHashMap<>(4) : existing.toSettings());

			// look for JSON on stdin if allowed
			if (!(ignoreStdIn || SystemUtils.systemConsoleIsTerminal())) {
				Map<String, Object> inputProps = objectMapper.readValue(new InputStreamReader(System.in, UTF_8),
						JsonUtils.STRING_MAP_TYPE);
				CollectionUtils.mergeServiceProperties(inputProps, settings, mode);
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
				CollectionUtils.mergeServiceProperties(inputProps, settings, mode);
			}

			if (!existing.differsFromSettings(settings)) {
				System.err.println("The settings have not changed, so no update performed.");
				return 0;
			}

			CloudDatumStreamConfiguration result;
			if (isDryRun()) {
				settings.put("configId", existing.configId());
				settings.put("created", existing.created());
				settings.put("modified", Instant.now());
				result = objectMapper.treeToValue(objectMapper.valueToTree(settings),
						CloudDatumStreamConfiguration.class);
			} else {
				result = updateCloudDatumStream(restClient, objectMapper, datumStreamId, settings);
			}

			List<?> tableData = (displayMode == ResultDisplayMode.JSON ? List.of(result)
					: List.of((Object) ListDatumStreamsCmd.tableDataRow(result, false, pretty)));
			TableUtils.renderTableData(ListDatumStreamsCmd.tableDataColumns(), tableData, displayMode, objectMapper,
					TableUtils.TableDataJsonPrettyPrinter.INSTANCE, System.out);
			return 0;
		} catch (Exception e) {
			System.err.println("Error viewing cloud datum streams: %s".formatted(e.getMessage()));
		}
		return 1;
	}

	private void populateSettings(Map<String, Object> settings) {
		if (name != null) {
			settings.put("name", name);
		}
		if (serviceIdentifier != null) {
			String type = CloudIntegrationsUtils.findDatumStreamServiceId(serviceIdentifier).getKey();
			settings.put("serviceIdentifier", type);
		}
		if (nodeOrLocationId != null) {
			if (nodeOrLocationId.isLocation()) {
				settings.put("kind", ObjectDatumKind.Location.keyValue());
				settings.put("objectId", nodeOrLocationId.locationId);
			} else {
				settings.put("kind", ObjectDatumKind.Node.keyValue());
				settings.put("objectId", nodeOrLocationId.nodeId);
			}
		}
		if (sourceId != null) {
			settings.put("sourceId", sourceId);
		}
		if (enabledOrDisabled != null) {
			settings.put("enabled", enabledOrDisabled.isEnabled());
		}
		if (mappingId != null) {
			settings.put("datumStreamMappingId", mappingId);
		}
		if (schedule != null) {
			settings.put("schedule", schedule);
		}

		if (serviceProperties != null) {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			final Map<String, Object> sprops = (Map) settings.compute(SERVICE_PROPERTIES_KEY,
					(_, v) -> v instanceof Map<?, ?> t ? (Map) t : new LinkedHashMap<>(8));

			CollectionUtils.populateServiceProperties(serviceProperties, sprops, objectMapper);
		}
	}

	private static final CloudDatumStreamConfiguration updateCloudDatumStream(RestClient restClient,
			ObjectMapper objectMapper, Long datumStreamId, Map<String, Object> settings) {
		// @formatter:off
		final JsonNode response = restClient.put()
				.uri(b -> {
					return b.path("/solaruser/api/v1/sec/user/c2c/datum-streams/{datumStreamId}")
							.build(datumStreamId);
				})
				.contentType(MediaType.APPLICATION_JSON)
				.body(settings)
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.body(JsonNode.class)
				;
		// @formatter:on

		checkSuccess(response);

		try {
			return objectMapper.treeToValue(response.path("data"), CloudDatumStreamConfiguration.class);
		} catch (JsonProcessingException | IllegalArgumentException e) {
			throw new IllegalStateException("Error parsing cloud datum stream response: " + e.getMessage(), e);
		}
	}

}
