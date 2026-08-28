package s10k.tool.c2c.mapping;

import static java.nio.charset.StandardCharsets.UTF_8;
import static s10k.tool.common.domain.ServiceConfiguration.SERVICE_PROPERTIES_KEY;
import static s10k.tool.common.util.RestUtils.checkSuccess;
import static s10k.tool.common.util.StringUtils.stringOrFileContents;
import static s10k.tool.common.util.TableUtils.tableConfig;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.jspecify.annotations.Nullable;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.solarnetwork.codec.JsonUtils;
import net.solarnetwork.domain.datum.DatumSamplesType;
import net.solarnetwork.util.DateUtils;
import net.solarnetwork.util.ObjectUtils;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import s10k.tool.c2c.domain.CloudDatumStreamMappingConfiguration;
import s10k.tool.c2c.domain.CloudDatumStreamMappingInfo;
import s10k.tool.c2c.domain.CloudDatumStreamMappingPropertyConfiguration;
import s10k.tool.c2c.domain.CloudDatumStreamMappingPropertyInfo;
import s10k.tool.c2c.domain.CloudDatumStreamValueType;
import s10k.tool.c2c.domain.CloudIntegrationConfiguration;
import s10k.tool.c2c.i9n.cmd.IntegrationsCmd;
import s10k.tool.c2c.util.CloudIntegrationRestUtils;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.MergeMode;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.util.CollectionUtils;
import s10k.tool.common.util.OutputUtils;
import s10k.tool.common.util.SystemUtils;
import s10k.tool.common.util.TableUtils;

/**
 * Create Cloud Datum Stream Mapping configurations.
 */
@Command(name = "create", sortSynopsis = false, showDefaultValues = true, descriptionHeading = "%n", optionListHeading = "%n", description = {
		// @formatter:off
		"Create a cloud datum stream mapping. The various options can be used to configure specific settings of the mapping.%n",

		"Alternatively the configuration can be provided as JSON via standard input or via an @file.json parameter.%n", 
		// @formatter:on
})
public class CreateMappingCmd extends BaseSubCmd<IntegrationsCmd> implements Callable<Integer> {

	// @formatter:off
	@Option(names = { "-i", "--integration-id" },
			description = "the ID of the integration to update")
	@Nullable Long integrationId;

	@Option(names = { "-m", "--name" },
			description = "the display name to assign")
	@Nullable String name;
	
	@Option(names = { "-prop", "--service-property" },
			description = "a service property, in the form path:value",
			paramLabel = "serviceProperty")
	String @Nullable [] serviceProperties;

	@Option(names = { "-p", "--property" },
			description = {
					"a mapping property, in the form [index,]type,name,val_type,ref[,multiplier][,scale]%n",
					
					"If the leading `index` is omitted, it will be assigned starting from 0 based on the order of the given -p options."
			},
			paramLabel = "propertyDefinition")
	String @Nullable [] propertyDefinitions;

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
	public CreateMappingCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
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
			} else if (!settings.containsKey("integrationId")) {
				System.err.println("An integration ID is required (--integration-id option).");
				return 1;
			}

			final Long integrationId = integrationId(settings);
			final CloudIntegrationConfiguration integration = CloudIntegrationRestUtils.viewCloudIntegration(restClient,
					objectMapper, integrationId);

			@SuppressWarnings({ "unchecked", "rawtypes" })
			final List<Map<String, Object>> propertySettings = (List) settings.remove("properties");

			// create mapping entity

			CloudDatumStreamMappingConfiguration mapping;
			List<CloudDatumStreamMappingPropertyConfiguration> properties = null;
			if (!isDryRun()) {
				mapping = createCloudDatumStreamMapping(restClient, objectMapper, settings);
				if (!propertySettings.isEmpty()) {
					properties = saveCloudDatumStreamMappingProperties(restClient, objectMapper, integrationId,
							propertySettings);
				}
			} else {
				settings.put("configId", -1L);
				String ts = DateUtils.ISO_DATE_TIME_ALT_UTC.format(Instant.now());
				settings.put("created", ts);
				settings.put("modified", ts);
				mapping = objectMapper.treeToValue(JsonUtils.getTreeFromObject(settings),
						CloudDatumStreamMappingConfiguration.class);
				if (!propertySettings.isEmpty()) {
					properties = new ArrayList<>(propertySettings.size());
					for (Map<String, Object> propSettings : propertySettings) {
						propSettings.put("datumStreamMappingId", -1L);
						propSettings.put("created", ts);
						propSettings.put("modified", ts);
						properties.add(objectMapper.treeToValue(JsonUtils.getTreeFromObject(propSettings),
								CloudDatumStreamMappingPropertyConfiguration.class));
					}
				}
			}

			if (displayMode == ResultDisplayMode.JSON) {
				OutputUtils.writeJsonObject(objectMapper,
						CloudDatumStreamMappingInfo.mappingPropertiesInfo(mapping, integration, properties));
			} else {
				final List<CloudDatumStreamMappingPropertyInfo> details = (properties == null || properties.isEmpty()
						? List.of(new CloudDatumStreamMappingPropertyInfo(mapping, integration, null))
						: properties.stream().map(p -> new CloudDatumStreamMappingPropertyInfo(mapping, integration, p))
								.toList());
				final Set<Long> lastSeenMapping = new HashSet<>(1);
				final List<?> tableData = details.stream().map(c -> ListMappingsCmd.tableDataRow(c, lastSeenMapping))
						.toList();
				TableUtils.renderTableData(ListMappingsCmd.tableDataColumns(), tableData,
						tableConfig(this, displayMode, prettyStyle()), objectMapper,
						TableUtils.TableDataJsonPrettyPrinter.INSTANCE, System.out);
			}
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
		if (integrationId != null) {
			settings.put("integrationId", integrationId);
		}
		if (serviceProperties != null) {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			final Map<String, Object> sprops = (Map) settings.compute(SERVICE_PROPERTIES_KEY,
					(_, v) -> v instanceof Map<?, ?> t ? (Map) t : new LinkedHashMap<>(8));
			CollectionUtils.populateServiceProperties(serviceProperties, sprops, objectMapper);
		}
		if (propertyDefinitions != null) {
			for (int i = 0, len = propertyDefinitions.length; i < len; i++) {
				Map<String, Object> propConfig = parsePropertyDefinition(i, propertyDefinitions[i]);
				@SuppressWarnings({ "unchecked", "rawtypes" })
				List<Map<String, Object>> props = (List) settings.compute("properties",
						(_, v) -> v instanceof List<?> t ? (List) t : new ArrayList<>(len));
				props.add(propConfig);
			}
		}
	}

	private Long integrationId(Map<String, Object> settings) {
		Object id = ObjectUtils.nonnull(settings.get("integrationId"), "Integration ID");
		if (id instanceof Long l) {
			return l;
		} else if (id instanceof Number n) {
			return n.longValue();
		} else {
			return Long.valueOf(id.toString());
		}
	}

	private Map<String, Object> parsePropertyDefinition(int index, String propertyDefinition) {
		// [index,]type,name,val_type,ref[,multiplier][,scale]
		String[] components = propertyDefinition.split(",", 0);
		if (components.length < 4) {
			throw new IllegalArgumentException(
					"At least type,name,val_type,ref components must be provided in a property definition.");
		}
		final Map<String, Object> settings = new LinkedHashMap<>(7);
		int pos = 0;

		Integer propIndex;
		if (components.length > 4) {
			try {
				propIndex = Integer.valueOf(components[0]);
				pos++;
			} catch (NumberFormatException e) {
				propIndex = index;
			}
		} else {
			propIndex = index;
		}
		settings.put("index", propIndex);
		settings.put("enabled", true);
		settings.put("propertyType", DatumSamplesType.fromValue(components[pos]));
		settings.put("propertyName", components[++pos]);
		settings.put("valueType", CloudDatumStreamValueType.fromValue(components[++pos]));
		settings.put("valueReference", components[++pos]);

		if (components.length > ++pos) {
			String s = components[pos];
			if (s != null && !s.isEmpty()) {
				try {
					settings.put("multiplier", new BigDecimal(s));
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException("Invalid multiplier value [%s].".formatted(s));
				}
			}
		}

		if (components.length > ++pos) {
			String s = components[pos];
			if (s != null && !s.isEmpty()) {
				try {
					settings.put("scale", Integer.valueOf(s));
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException("Invalid scale value [%s].".formatted(s));
				}
			}
		}

		return settings;
	}

	private static CloudDatumStreamMappingConfiguration createCloudDatumStreamMapping(RestClient restClient,
			ObjectMapper objectMapper, Map<String, Object> settings) {
		// @formatter:off
		JsonNode response = checkSuccess(restClient.post()
			.uri(b -> {
				b.path("/solaruser/api/v1/sec/user/c2c/datum-stream-mappings");
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
			return objectMapper.treeToValue(response.path("data"), CloudDatumStreamMappingConfiguration.class);
		} catch (JsonProcessingException | IllegalArgumentException e) {
			throw new IllegalStateException(
					"Error parsing cloud datum stream mapping create response: " + e.getMessage(), e);
		}
	}

	/**
	 * Save cloud datum stream mapping properties for a mapping entity.
	 * 
	 * @param restClient   the REST client
	 * @param objectMapper the mapper
	 * @param mappingId    the mapping ID
	 * @param settings     the mapping property settings to save
	 */
	public static List<CloudDatumStreamMappingPropertyConfiguration> saveCloudDatumStreamMappingProperties(
			RestClient restClient, ObjectMapper objectMapper, Long mappingId, List<Map<String, Object>> settings) {
		// @formatter:off
		JsonNode response = checkSuccess(restClient.post()
			.uri(b -> {
				b.path("/solaruser/api/v1/sec/user/c2c/datum-stream-mappings/{datumStreamMappingId}/properties");
				return b.build(mappingId);
			})
			.contentType(MediaType.APPLICATION_JSON)
			.body(settings)
			.accept(MediaType.APPLICATION_JSON)
			.retrieve()
			.body(JsonNode.class)
			);		
		// @formatter:on

		try {
			CloudDatumStreamMappingPropertyConfiguration[] result = objectMapper.treeToValue(response.path("data"),
					CloudDatumStreamMappingPropertyConfiguration[].class);
			return (result != null ? List.of(result) : List.of());
		} catch (JsonProcessingException | IllegalArgumentException e) {
			throw new IllegalStateException(
					"Error parsing cloud datum stream mapping create response: " + e.getMessage(), e);
		}
	}

}
