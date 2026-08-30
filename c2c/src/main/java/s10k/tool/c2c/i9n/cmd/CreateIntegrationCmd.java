package s10k.tool.c2c.i9n.cmd;

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
import net.solarnetwork.util.DateUtils;
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
 * Create Cloud Integration configurations.
 */
@Command(name = "create", sortSynopsis = false, showDefaultValues = true, descriptionHeading = "%n", optionListHeading = "%n", description = {
		// @formatter:off
		"""
		Create a cloud integration. The various options can be used to configure specific \
		settings of the integration.
		""",

		"""
		Alternatively the configuration can be provided as JSON via standard input or via \
		an @file.json parameter. The JSON must be structured as an object as specified in \
		the @|bold Cloud Integration create|@ API in SolarNetwork.
		""",
		// @formatter:on
})
public class CreateIntegrationCmd extends BaseSubCmd<IntegrationsGroup> implements Callable<Integer> {

	// @formatter:off
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

	@Option(names = {"-d", "--disabled"},
			description = "craete in disabled state")
	boolean disabled;

	@Option(names = { "-g", "--merge-mode" },
			description = "the merge style to perform",
			defaultValue = "RecursiveObjects")
	@SuppressWarnings("NullAway.Init")
	MergeMode mode;

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
	 * Constructor.
	 * 
	 * @param reqFactory   the HTTP request factory to use
	 * @param objectMapper the mapper to use
	 */
	public CreateIntegrationCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		try {
			final RestClient restClient = restClient();
			final ObjectMapper objectMapper = objectMapper();
			final ResultDisplayMode displayMode = displayMode(this.displayMode);

			final Map<String, Object> settings = new LinkedHashMap<>(4);

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
				conf = createCloudIntegration(restClient, objectMapper, settings);
			} else {
				settings.put("configId", -1L);
				String ts = DateUtils.ISO_DATE_TIME_ALT_UTC.format(Instant.now());
				settings.put("created", ts);
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
			System.err.println("Error creating cloud integration: %s".formatted(e.getMessage()));
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
		settings.put("enabled", !disabled);
		if (serviceProperties != null) {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			final Map<String, Object> sprops = (Map) settings.compute(SERVICE_PROPERTIES_KEY,
					(_, v) -> v instanceof Map<?, ?> t ? (Map) t : new LinkedHashMap<>(8));
			CollectionUtils.populateServiceProperties(serviceProperties, sprops, objectMapper);
		}
	}

	private static CloudIntegrationConfiguration createCloudIntegration(RestClient restClient,
			ObjectMapper objectMapper, Map<String, Object> settings) {
		// @formatter:off
		JsonNode response = checkSuccess(restClient.post()
			.uri(b -> {
				b.path("/solaruser/api/v1/sec/user/c2c/integrations");
				return b.build();
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
			throw new IllegalStateException("Error parsing cloud integration create response: " + e.getMessage(), e);
		}
	}

}
