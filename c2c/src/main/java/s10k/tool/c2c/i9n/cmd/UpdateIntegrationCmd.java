package s10k.tool.c2c.i9n.cmd;

import static java.nio.charset.StandardCharsets.UTF_8;
import static s10k.tool.c2c.util.CloudIntegrationRestUtils.viewCloudIntegration;
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
import net.solarnetwork.util.DateUtils;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import s10k.tool.c2c.domain.CloudIntegrationConfiguration;
import s10k.tool.c2c.util.CloudIntegrationsUtils;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.MergeMode;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.util.CollectionUtils;
import s10k.tool.common.util.SystemUtils;
import s10k.tool.common.util.TableUtils;

/**
 * Update Cloud Integration configurations.
 */
@Command(name = "update", sortSynopsis = false, showDefaultValues = true, descriptionHeading = "%n", optionListHeading = "%n", description = {
		// @formatter:off
		"Update a cloud integration. The various optional options can be used to update",
		"specific settings of an integration, leaving all other settings of the integration",
		"unchagned.%n",

		"Alternatively the configuration can be provided as JSON via standard input or via",
		"an @file.json parameter. The JSON must be structured as an object as specified",
		"in the @|bold Cloud Integration update|@ API in SolarNetwork. The given configuration",
		"will be merged into the existing configuration unless the @|bold --replace|@ option is given",
		"in which case the given JSON will completely replace the existing configuration.%n", 
		// @formatter:on
})
public class UpdateIntegrationCmd extends BaseSubCmd<IntegrationsCmd> implements Callable<Integer> {

	// @formatter:off
	@Option(names = { "-i", "--integration-id" },
			description = "the ID of the integration to update",
			required =  true)
	@SuppressWarnings("NullAway.Init")
	Long integrationId;

	@Option(names = { "-g", "--merge-mode" },
			description = "the merge style to perform",
			defaultValue = "RecursiveObjects")
	@SuppressWarnings("NullAway.Init")
	MergeMode mode;

	@Option(names = { "-m", "--name" },
			description = "the display name to assign")
	@Nullable String name;
	
	@Option(names = { "-S", "--service" },
			description = "the integration service identifier")
	@Nullable String serviceIdentifier;

	@Option(names = { "-prop", "--service-property" },
			description = "a service property, in the form path:value",
			paramLabel = "serviceProperty")
	String @Nullable [] serviceProperties;

	@ArgGroup(exclusive = true, multiplicity = "0..1")
	@Nullable EnabledOrDisabled enabledOrDisabled;

	@Option(names = {"-r", "--replace"},
			description = "when JSON input is provided, replace the settings instead of merging the given settings")
	public boolean replace;
	
	@Option(names = {"-I", "--ignore-input"},
			description = "do not try to read settings from standard input")
	boolean ignoreStdIn;
	
	@Option(names = { "-mode", "--display-mode" },
			description = "how to display the data")
	@Nullable ResultDisplayMode displayMode;

	@Parameters(index = "0",
			arity = "0..1",
			paramLabel = "<config>",
			description = "the configuration to use as a JSON object, or @file for file to load")
	@Nullable String value;
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
	 * Constructor.
	 * 
	 * @param reqFactory   the HTTP request factory to use
	 * @param objectMapper the mapper to use
	 */
	public UpdateIntegrationCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		try {
			final RestClient restClient = restClient();
			final ObjectMapper objectMapper = objectMapper();
			final ResultDisplayMode displayMode = displayMode(this.displayMode);

			final CloudIntegrationConfiguration existing = viewCloudIntegration(restClient, objectMapper,
					integrationId);

			final Map<String, Object> settings = (replace ? new LinkedHashMap<>(4) : existing.toSettings());

			// look for JSON on stdin if allowed
			if (!(ignoreStdIn || SystemUtils.systemConsoleIsTerminal())) {
				Map<String, Object> inputProps = objectMapper.readValue(new InputStreamReader(System.in, UTF_8),
						JsonUtils.STRING_MAP_TYPE);
				CollectionUtils.mergeServiceProperties(inputProps, settings, mode);
			}

			try {
				populateConfiguration(settings, objectMapper);
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

			CloudIntegrationConfiguration conf;
			if (!isDryRun()) {
				conf = updateCloudIntegration(restClient, objectMapper, integrationId, settings);
			} else {
				String ts = DateUtils.ISO_DATE_TIME_ALT_UTC.format(Instant.now());
				settings.put("modified", ts);
				conf = objectMapper.treeToValue(JsonUtils.getTreeFromObject(settings),
						CloudIntegrationConfiguration.class);
			}

			final List<CloudIntegrationConfiguration> confs = List.of(conf);
			List<?> tableData = (displayMode == ResultDisplayMode.JSON ? confs
					: confs.stream().map(c -> ListIntegrationsCmd.tableDataRow(c, false)).toList());
			TableUtils.renderTableData(ListIntegrationsCmd.tableDataColumns(), tableData,
					tableConfig(this, displayMode, prettyStyle()).asJsonSingleton(), objectMapper,
					TableUtils.TableDataJsonPrettyPrinter.INSTANCE, System.out);
			return 0;
		} catch (Exception e) {
			System.err.println("Error updating cloud integration: %s".formatted(e.getMessage()));
		}
		return 1;
	}

	private void populateConfiguration(Map<String, Object> settings, ObjectMapper objectMapper) {
		if (name != null) {
			settings.put("name", name);
		}
		if (serviceIdentifier != null) {
			String type = CloudIntegrationsUtils.findIntegrationServiceId(serviceIdentifier).getKey();
			settings.put("serviceIdentifier", type);
		}
		if (enabledOrDisabled != null) {
			settings.put("enabled", enabledOrDisabled.isEnabled());
		}
		if (serviceProperties != null) {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			final Map<String, Object> sprops = (Map) settings.compute(SERVICE_PROPERTIES_KEY,
					(_, v) -> v instanceof Map<?, ?> t ? (Map) t : new LinkedHashMap<>(8));
			CollectionUtils.populateServiceProperties(serviceProperties, sprops, objectMapper);
		}
	}

	private static CloudIntegrationConfiguration updateCloudIntegration(RestClient restClient,
			ObjectMapper objectMapper, Long integrationId, Map<String, Object> settings) {
		// @formatter:off
		JsonNode response = checkSuccess(restClient.put()
			.uri(b -> {
				b.path("/solaruser/api/v1/sec/user/c2c/integrations/{integrationId}");
				return b.build(integrationId);
			})
			.contentType(MediaType.APPLICATION_JSON)
			.body(settings)
			.accept(MediaType.APPLICATION_JSON)
			.retrieve()
			.body(JsonNode.class)
			);		
		// @formatter:on

		try {
			return objectMapper.treeToValue(response.path("data"), CloudIntegrationConfiguration.class);
		} catch (JsonProcessingException | IllegalArgumentException e) {
			throw new IllegalStateException("Error parsing cloud integration update response: " + e.getMessage(), e);
		}
	}

}
