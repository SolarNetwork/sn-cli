package s10k.tool.c2c.i9n.cmd;

import static com.github.freva.asciitable.HorizontalAlign.LEFT;
import static com.github.freva.asciitable.HorizontalAlign.RIGHT;
import static s10k.tool.c2c.util.CloudIntegrationsUtils.integrationServiceLocalizedName;
import static s10k.tool.common.util.RestUtils.checkSuccess;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.freva.asciitable.Column;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.util.TableUtils;

/**
 * View Cloud Integration configurations.
 */
@Component
@Command(name = "list", sortSynopsis = false)
public class ListIntegrationsCmd extends BaseSubCmd<IntegrationsCmd> implements Callable<Integer> {

	// @formatter:off
	@Option(names = { "-mode", "--display-mode" },
			description = "how to display the data",
			defaultValue = "PRETTY")
	ResultDisplayMode displayMode = ResultDisplayMode.PRETTY;
	// @formatter:on

	/**
	 * Constructor.
	 * 
	 * @param reqFactory   the HTTP request factory to use
	 * @param objectMapper the mapper to use
	 */
	public ListIntegrationsCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		final RestClient restClient = restClient();

		try {
			List<CloudIntegrationConfiguration> confs = listCloudIntegrations(restClient, objectMapper);
			if (confs.isEmpty()) {
				System.err.println("No sources matched your criteria.");
				return 0;
			}

			List<?> tableData = (displayMode == ResultDisplayMode.JSON ? confs
					: confs.stream().map(c -> tableDataRow(c, false)).toList());
			TableUtils.renderTableData(tableDataColumns(), tableData, displayMode, objectMapper,
					TableUtils.TableDataJsonPrettyPrinter.INSTANCE, System.out);
			return 0;
		} catch (Exception e) {
			System.err.println("Error viewing cloud integrations: %s".formatted(e.getMessage()));
		}
		return 1;
	}

	/**
	 * Get integrations info tabular structure columns.
	 * 
	 * @return the columns
	 */
	public static Column[] tableDataColumns() {
		// @formatter:off
		return new Column[] {
				new Column().header("ID").dataAlign(RIGHT),
				new Column().header("Name").dataAlign(LEFT),
				new Column().header("Kind").dataAlign(LEFT),
				new Column().header("Enabled").dataAlign(LEFT),
			};
		// @formatter:on
	}

	/**
	 * Convert integrations listing into a tabular structure.
	 * 
	 * @param conf           the configuration to convert
	 * @param rawIdentifiers {@code true} to output the raw service identifiers
	 * @return the metadata data
	 */
	public static Object[] tableDataRow(CloudIntegrationConfiguration conf, boolean rawIdentifiers) {
		// @formatter:off
		return new Object[] {
				conf.configId(),
				conf.name(),
				(rawIdentifiers ? conf.serviceIdentifier() : integrationServiceLocalizedName(conf.serviceIdentifier())),
				conf.enabled(),
			};
		// @formatter:on
	}

	/**
	 * List cloud integrations.
	 * 
	 * @param restClient   the REST client
	 * @param objectMapper the object mapper
	 * @return the result
	 */
	public static List<CloudIntegrationConfiguration> listCloudIntegrations(RestClient restClient,
			ObjectMapper objectMapper) {
		// @formatter:off
		JsonNode response = restClient.get()
			.uri("/solaruser/api/v1/sec/user/c2c/integrations")
			.accept(MediaType.APPLICATION_JSON)
			.retrieve()
			.body(JsonNode.class)
			;		
		// @formatter:on

		checkSuccess(response);

		List<CloudIntegrationConfiguration> result = new ArrayList<>(response.path("data").size());
		for (JsonNode node : response.path("data").path("results")) {
			CloudIntegrationConfiguration conf;
			try {
				conf = objectMapper.treeToValue(node, CloudIntegrationConfiguration.class);
			} catch (JsonProcessingException | IllegalArgumentException e) {
				throw new IllegalStateException("Error parsing cloud integration list response: " + e.getMessage(), e);
			}
			if (conf != null) {
				result.add(conf);
			}
		}
		return result;
	}

}
