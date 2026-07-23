package s10k.tool.datum.imp.cmd;

import static s10k.tool.common.util.RestUtils.checkSuccess;
import static s10k.tool.datum.imp.domain.DatumImportState.Queued;

import java.util.List;
import java.util.concurrent.Callable;

import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.util.TableUtils;
import s10k.tool.datum.imp.domain.DatumImportTaskInfo;
import s10k.tool.datum.imp.util.DatumImportRestUtils;

/**
 * Confirm a staged datum import.
 */
@Command(name = "confirm-staged", aliases = "confirm", sortSynopsis = false, descriptionHeading = "%n", optionListHeading = "%n", description = {
		"Confirm a @|bold staged|@ datum import job so it may being the import process.%n",

		"Once confirmed, the job will transition to the @|bold Queued|@ state, and begin processing",
		"at some point in the future.%n" })
public class ConfirmStagedImportCmd extends BaseSubCmd<DatumImportsCmd> implements Callable<Integer> {

	// @formatter:off
		@Option(names = { "-j", "--job-id" },
				description = "the staged job to confirm",
				required = true)
		String jobId;

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
	public ConfirmStagedImportCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		final RestClient restClient = restClient();
		try {

			final DatumImportTaskInfo result;

			if (isDryRun()) {
				result = DatumImportRestUtils.viewDatumImportTask(restClient, objectMapper, jobId)
						.copyWithState(Queued);
			} else {
				result = confirmStagedDatumImportTask(restClient, objectMapper, jobId);
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

	/**
	 * Confirm a staged datum import task.
	 * 
	 * @param restClient   the REST client
	 * @param objectMapper the object mapper
	 * @param jobId        the staged job ID to preview
	 * @return the updated job info
	 * @throws IllegalStateException if an error occurs
	 */
	public static DatumImportTaskInfo confirmStagedDatumImportTask(RestClient restClient, ObjectMapper objectMapper,
			String jobId) {
		// @formatter:off
			final JsonNode response = checkSuccess(restClient.post()
					.uri(b -> {
						b.path("/solaruser/api/v1/sec/user/import/jobs/{jobId}/confirm");
						return b.build(jobId);
					})
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.body(JsonNode.class)
				);		
			// @formatter:on

		try {
			return objectMapper.treeToValue(response.path("data"), DatumImportTaskInfo.class);
		} catch (JsonProcessingException | IllegalArgumentException e) {
			throw new IllegalStateException("Error parsing confirm datum import response: " + e.getMessage(), e);
		}
	}

}
