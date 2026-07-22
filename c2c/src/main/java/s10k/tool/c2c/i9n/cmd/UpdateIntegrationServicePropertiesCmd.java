package s10k.tool.c2c.i9n.cmd;

import static java.nio.charset.StandardCharsets.UTF_8;
import static s10k.tool.c2c.util.CloudIntegrationRestUtils.viewCloudIntegration;
import static s10k.tool.common.util.RestUtils.checkSuccess;
import static s10k.tool.common.util.StringUtils.stringOrFileContents;

import java.io.InputStreamReader;
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

import net.solarnetwork.codec.JsonUtils;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import s10k.tool.c2c.domain.CloudIntegrationConfiguration;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.MergeMode;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.util.CollectionUtils;
import s10k.tool.common.util.SystemUtils;
import s10k.tool.common.util.TableUtils;

/**
 * Update cloud integration entity service properties.
 */
@Component
@Command(name = "update-service-properties", aliases = "update-props", sortSynopsis = false, descriptionHeading = "%n", optionListHeading = "%n", description = {
		"Update a cloud integration's service properties using a @|bold merge|@ operation.%n" })
public class UpdateIntegrationServicePropertiesCmd extends BaseSubCmd<IntegrationsCmd> implements Callable<Integer> {

	// @formatter:off
	@Option(names = { "-i", "--integration-id" },
			description = "the ID of the integration to update",
			required =  true)
	Long integrationId;
	
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

	@Parameters(index = "0", paramLabel = "<config>", description = "the properties to save, or @file for file to load", arity = "0..1")
	String value;
	// @formatter:on

	/**
	 * Constructor.
	 * 
	 * @param reqFactory   the HTTP request factory to use
	 * @param objectMapper the mapper to use
	 */
	public UpdateIntegrationServicePropertiesCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		final RestClient restClient = restClient();
		try {
			final CloudIntegrationConfiguration existing;
			if (isDryRun()) {
				existing = viewCloudIntegration(restClient, objectMapper, integrationId);
			} else {
				existing = null;
			}

			final Map<String, Object> settings = (existing != null ? existing.serviceProperties()
					: new LinkedHashMap<>(4));

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

			Map<String, Object> result;
			if (isDryRun()) {
				result = settings;
			} else {
				result = updateCloudIntegrationServiceProperties(restClient, objectMapper, integrationId, mode,
						settings);
			}

			TableUtils.renderTableData(TableUtils.mapColumns("Property", "Value", false), List.of(result), displayMode,
					objectMapper, TableUtils.TableDataJsonPrettyPrinter.INSTANCE, System.out);
			return 0;
		} catch (Exception e) {
			System.err.println("Error viewing cloud datum streams: %s".formatted(e.getMessage()));
		}
		return 1;
	}

	private void populateSettings(Map<String, Object> settings) {
		CollectionUtils.populateServiceProperties(serviceProperties, settings, objectMapper);
	}

	private static final Map<String, Object> updateCloudIntegrationServiceProperties(RestClient restClient,
			ObjectMapper objectMapper, Long integrationId, MergeMode mode, Map<String, Object> serviceProperties) {
		// @formatter:off
		final JsonNode response = restClient.patch()
				.uri(b -> {
					return b.path("/solaruser/api/v1/sec/user/c2c/integrations/{integrationId}/serviceProperties")
						.queryParam("mode", mode)
						.build(integrationId);
				})
				.contentType(MediaType.APPLICATION_JSON)
				.body(serviceProperties)
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.body(JsonNode.class)
				;
		// @formatter:on

		checkSuccess(response);

		try {
			return objectMapper.treeToValue(response.path("data"), JsonUtils.STRING_MAP_TYPE);
		} catch (JsonProcessingException | IllegalArgumentException e) {
			throw new IllegalStateException("Error parsing service properties response: " + e.getMessage(), e);
		}
	}

}
