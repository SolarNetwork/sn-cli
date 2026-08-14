package s10k.tool.c2c.i9n.cmd;

import static s10k.tool.c2c.util.CloudIntegrationRestUtils.listCloudIntegrations;
import static s10k.tool.common.util.RestUtils.checkSuccess;
import static s10k.tool.common.util.TableUtils.tableConfig;

import java.util.List;
import java.util.concurrent.Callable;

import org.jspecify.annotations.Nullable;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import s10k.tool.c2c.domain.CloudIntegrationConfiguration;
import s10k.tool.c2c.domain.CloudIntegrationsFilter;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.util.TableUtils;

/**
 * Delete Cloud Integration configurations.
 */
@Command(name = "delete", sortSynopsis = false, showDefaultValues = true, descriptionHeading = "%n", optionListHeading = "%n", description = {
		"Delete Cloud Integration entities.%n" })
public class DeleteIntegrationsCmd extends BaseSubCmd<IntegrationsCmd> implements Callable<Integer> {

	// @formatter:off
	@Option(names = { "-i", "--integration-id" },
			description = "an integration ID to match",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "integrationId")
	@SuppressWarnings("NullAway.Init")
	Long[] integrationIds;
	
	@Option(names = { "-mode", "--display-mode" },
			description = "how to display the data")
	@Nullable ResultDisplayMode displayMode;
	// @formatter:on

	/**
	 * Constructor.
	 * 
	 * @param reqFactory   the HTTP request factory to use
	 * @param objectMapper the mapper to use
	 */
	public DeleteIntegrationsCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		try {
			final RestClient restClient = restClient();
			final ObjectMapper objectMapper = objectMapper();
			final ResultDisplayMode displayMode = displayMode(this.displayMode);

			final var filter = new CloudIntegrationsFilter();
			filter.setIntegrationIds(List.of(integrationIds));

			final List<CloudIntegrationConfiguration> confs = listCloudIntegrations(restClient, objectMapper, filter);
			if (confs.isEmpty()) {
				System.err.println("No integrations matched your criteria.");
				return 0;
			}

			if (!isDryRun()) {
				for (CloudIntegrationConfiguration stream : confs) {
					deleteCloudIntegration(restClient, objectMapper, stream.configId());
				}
			}

			List<?> tableData = (displayMode == ResultDisplayMode.JSON ? confs
					: confs.stream().map(c -> ListIntegrationsCmd.tableDataRow(c, false)).toList());
			TableUtils.renderTableData(ListIntegrationsCmd.tableDataColumns(), tableData,
					tableConfig(this, displayMode, prettyStyle()), objectMapper, TableUtils.TableDataJsonPrettyPrinter.INSTANCE,
					System.out);
			return 0;
		} catch (Exception e) {
			System.err.println("Error deleting cloud integrations: %s".formatted(e.getMessage()));
		}
		return 1;
	}

	/**
	 * Delete a cloud integration.
	 * 
	 * @param restClient    the REST client
	 * @param objectMapper  the object mapper
	 * @param integrationId the integration ID to delete
	 * @throws IllegalStateException if an error occurs fetching the stream
	 */
	public static void deleteCloudIntegration(RestClient restClient, ObjectMapper objectMapper, Long integrationId) {
		// @formatter:off
		checkSuccess(restClient.delete()
			.uri(b -> b.path("/solaruser/api/v1/sec/user/c2c/integrations/{integrationId}")
				.build(integrationId)
			)
			.accept(MediaType.APPLICATION_JSON)
			.retrieve()
			.body(JsonNode.class)
			);		
		// @formatter:on
	}

}
