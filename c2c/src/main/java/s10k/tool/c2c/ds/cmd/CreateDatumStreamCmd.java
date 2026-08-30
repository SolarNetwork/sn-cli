package s10k.tool.c2c.ds.cmd;

import static java.nio.charset.StandardCharsets.UTF_8;
import static s10k.tool.common.domain.ServiceConfiguration.SERVICE_PROPERTIES_KEY;
import static s10k.tool.common.util.RestUtils.checkSuccess;
import static s10k.tool.common.util.StringUtils.stringOrFileContents;
import static s10k.tool.common.util.TableUtils.tableConfig;

import java.io.InputStreamReader;
import java.time.Instant;
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
import net.solarnetwork.domain.datum.ObjectDatumKind;
import net.solarnetwork.util.DateUtils;
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
 * Create Cloud Datum Stream configurations.
 */
@Command(name = "create", sortSynopsis = false, showDefaultValues = true, descriptionHeading = "%n", optionListHeading = "%n", description = {
		// @formatter:off
		"""
		Create a cloud datum stream. The various options can be used to configure specific \
		settings of the datum stream.
		""",
		
		"""
		Alternatively the configuration can be provided as JSON via standard input or via \
		an @file.json parameter. The JSON must be structured as an object as specified \
		in the @|bold Cloud Datum Stream create|@ API in SolarNetwork.
		""",
		// @formatter:on
})
public class CreateDatumStreamCmd extends BaseSubCmd<DatumStreamsGroup> implements Callable<Integer> {

	// @formatter:off
	@Option(names = { "-g", "--merge-mode" },
			description = "the merge style to perform",
			defaultValue = "RecursiveObjects")
	MergeMode mode;

	@Option(names = { "-S", "--service" },
			description = "the service identifier to set; a substring of the service type can be used")
	@Nullable String serviceIdentifier;

	@Option(names = { "-m", "--name" },
			description = "a name to set")
	@Nullable String name;

	@ArgGroup(exclusive = true, multiplicity = "0..1")
	@Nullable NodeOrLocationId nodeOrLocationId;

	@Option(names = { "-source", "--source-id" },
			description = "the source ID to set")
	@Nullable String sourceId;
	
	@Option(names = {"-d", "--disabled"},
			description = "craete in disabled state")
	boolean disabled;

	@Option(names = { "-map", "--mapping-id" },
			description = "the datum  stream mapping ID to set")
	Long mappingId;

	@Option(names = { "-w", "--schedule" },
			description = "the schedule to set; should be a cron schedule or a number of seconds")
	String schedule;
	
	@Option(names = { "-prop", "--service-property" },
			description = "a service property, in the form path:value",
			paramLabel = "serviceProperty")
	String @Nullable [] serviceProperties;

	@Option(names = {"-I", "--ignore-input"},
			description = "do not try to read settings from standard input")
	boolean ignoreStdIn;
	
	@Option(names = { "-mode", "--display-mode" },
			description = "how to display the data")
	ResultDisplayMode displayMode;

	@Parameters(index = "0", paramLabel = "<config>", description = "the updates to save, or @file for file to load", arity = "0..1")
	String value;
	// @formatter:on

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
	public CreateDatumStreamCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		try {
			final RestClient restClient = restClient();
			final ResultDisplayMode displayMode = displayMode(this.displayMode);

			final Map<String, Object> settings = new LinkedHashMap<>(4);

			// look for JSON on stdin if allowed
			if (!(ignoreStdIn || SystemUtils.systemConsoleIsTerminal())) {
				Map<String, Object> inputProps = objectMapper.readValue(new InputStreamReader(System.in, UTF_8),
						JsonUtils.STRING_MAP_TYPE);
				CollectionUtils.mergeServiceProperties(inputProps, settings, mode);
			}

			try {
				populateConfiguration(settings);
			} catch (RuntimeException e) {
				System.err.println(e.getMessage());
				return 1;
			}

			if (value != null && !value.isBlank()) {
				Map<String, Object> inputProps = objectMapper.readValue(stringOrFileContents(value),
						JsonUtils.STRING_MAP_TYPE);
				CollectionUtils.mergeServiceProperties(inputProps, settings, mode);
			}

			if (!settings.containsKey("name")) {
				System.err.println("A name is required (--name option).");
				return 1;
			} else if (!settings.containsKey("serviceIdentifier")) {
				System.err.println("A service identifier is required (--service option).");
				return 1;
			}

			CloudDatumStreamConfiguration conf;
			if (isDryRun()) {
				settings.put("configId", -1L);
				String ts = DateUtils.ISO_DATE_TIME_ALT_UTC.format(Instant.now());
				settings.put("created", ts);
				settings.put("modified", ts);
				conf = objectMapper.treeToValue(JsonUtils.getTreeFromObject(settings),
						CloudDatumStreamConfiguration.class);
			} else {
				conf = createCloudDatumStream(restClient, objectMapper, settings);
			}

			List<?> tableData = (displayMode == ResultDisplayMode.JSON ? List.of(conf)
					: List.of((Object) ListDatumStreamsCmd.tableDataRow(conf, false)));
			TableUtils.renderTableData(ListDatumStreamsCmd.tableDataColumns(), tableData,
					tableConfig(this, displayMode, prettyStyle()).asJsonSingleton(), objectMapper,
					TableUtils.TableDataJsonPrettyPrinter.INSTANCE, System.out);
			return 0;
		} catch (Exception e) {
			System.err.println("Error creating cloud datum stream: %s".formatted(e.getMessage()));
		}
		return 1;
	}

	private void populateConfiguration(Map<String, Object> settings) {
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
		} else {
			settings.put("kind", ObjectDatumKind.Node.keyValue());
		}
		if (sourceId != null) {
			settings.put("sourceId", sourceId);
		}
		settings.put("enabled", !disabled);
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

	private static final CloudDatumStreamConfiguration createCloudDatumStream(RestClient restClient,
			ObjectMapper objectMapper, Map<String, Object> settings) {
		// @formatter:off
		final JsonNode response = checkSuccess(restClient.post()
				.uri(b -> {
					return b.path("/solaruser/api/v1/sec/user/c2c/datum-streams")
							.build();
				})
				.contentType(MediaType.APPLICATION_JSON)
				.body(settings)
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.body(JsonNode.class)
				);
		// @formatter:on

		try {
			return objectMapper.treeToValue(response.path("data"), CloudDatumStreamConfiguration.class);
		} catch (JsonProcessingException | IllegalArgumentException e) {
			throw new IllegalStateException("Error parsing cloud datum stream response: " + e.getMessage(), e);
		}
	}

}
