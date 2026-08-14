package s10k.tool.c2c.ds.cmd;

import static s10k.tool.c2c.util.CloudIntegrationRestUtils.listCloudDatumStreams;
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
import com.fasterxml.jackson.databind.ObjectWriter;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import s10k.tool.c2c.domain.CloudDatumStreamConfiguration;
import s10k.tool.c2c.domain.CloudIntegrationsFilter;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.util.TableUtils;

/**
 * Delete Cloud Datum Stream configurations.
 */
@Command(name = "delete", sortSynopsis = false, showDefaultValues = true, descriptionHeading = "%n", optionListHeading = "%n", description = {
		"Delete Cloud Datum Stream entities.%n" })
public class DeleteDatumStreamsCmd extends BaseSubCmd<DatumStreamsCmd> implements Callable<Integer> {

	// @formatter:off
	@Option(names = { "-stream", "--stream-id" },
			description = "a datum stream ID to delete",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "datumStreamId",
			required = true)
	@SuppressWarnings("NullAway.Init")
	Long[] datumStreamIds;

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
	public DeleteDatumStreamsCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		try {
			final RestClient restClient = restClient();
			final ResultDisplayMode displayMode = displayMode(this.displayMode);
			final ObjectMapper objectMapper = objectMapper();
			final ObjectWriter pretty = objectMapper.writerWithDefaultPrettyPrinter();

			final var filter = new CloudIntegrationsFilter();
			filter.setDatumStreamIds(List.of(datumStreamIds));

			final List<CloudDatumStreamConfiguration> confs = listCloudDatumStreams(restClient, objectMapper, filter);
			if (confs.isEmpty()) {
				System.err.println("No datum streams matched your criteria.");
				return 0;
			}

			if (!isDryRun()) {
				for (CloudDatumStreamConfiguration stream : confs) {
					deleteCloudDatumStream(restClient, objectMapper, stream.configId());
				}
			}

			List<?> tableData = (displayMode == ResultDisplayMode.JSON ? confs
					: confs.stream().map(c -> ListDatumStreamsCmd.tableDataRow(c, false, pretty)).toList());
			TableUtils.renderTableData(ListDatumStreamsCmd.tableDataColumns(), tableData,
					tableConfig(this, displayMode, prettyStyle()), objectMapper, TableUtils.TableDataJsonPrettyPrinter.INSTANCE,
					System.out);
			return 0;
		} catch (Exception e) {
			System.err.println("Error deleting cloud datum streams: %s".formatted(e.getMessage()));
		}
		return 1;
	}

	/**
	 * Delete a cloud datum stream.
	 * 
	 * @param restClient    the REST client
	 * @param objectMapper  the object mapper
	 * @param datumStreamId the datum stream ID to delete
	 * @throws IllegalStateException if an error occurs fetching the stream
	 */
	public static void deleteCloudDatumStream(RestClient restClient, ObjectMapper objectMapper, Long datumStreamId) {
		// @formatter:off
		checkSuccess(restClient.delete()
			.uri(b -> b.path("/solaruser/api/v1/sec/user/c2c/datum-streams/{datumStreamId}")
				.build(datumStreamId)
			)
			.accept(MediaType.APPLICATION_JSON)
			.retrieve()
			.body(JsonNode.class)
			);		
		// @formatter:on
	}

}
