package s10k.tool.datum.del.cmd;

import static com.github.freva.asciitable.HorizontalAlign.LEFT;
import static com.github.freva.asciitable.HorizontalAlign.RIGHT;
import static s10k.tool.common.util.DateUtils.nonEpochInstant;
import static s10k.tool.common.util.RestUtils.checkSuccess;
import static s10k.tool.common.util.StringUtils.onlyTrueValue;
import static s10k.tool.common.util.TableUtils.tableConfig;

import java.util.List;
import java.util.concurrent.Callable;

import org.jspecify.annotations.Nullable;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
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
import s10k.tool.datum.del.domain.DatumDeleteTaskInfo;
import s10k.tool.datum.imp.cmd.DatumImportsCmd;

/**
 * View datum import job status.
 */
@Command(name = "view", sortSynopsis = false, showDefaultValues = true, descriptionHeading = "%n", optionListHeading = "%n", description = {
		"View the details of a previously submitted datum delete job.%n" })
public class ViewDeleteJobCmd extends BaseSubCmd<DatumImportsCmd> implements Callable<Integer> {

	// @formatter:off
	@Option(names = { "-j", "--job-id" },
			description = "the ID of the job to view",
			required = true)
	@SuppressWarnings("NullAway.Init")
	String jobId;

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
	public ViewDeleteJobCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		final RestClient restClient = restClient();
		final ObjectMapper objectMapper = objectMapper();
		final ResultDisplayMode displayMode = displayMode(this.displayMode);
		try {
			final DatumDeleteTaskInfo result = viewDatumDeleteTask(restClient, objectMapper, jobId);

			List<?> tableData = (displayMode == ResultDisplayMode.JSON ? List.of(result)
					: List.of((Object) tableDataRow(result)));
			TableUtils.renderTableData(tableDataColumns(), tableData, tableConfig(this, displayMode).asJsonSingleton(),
					objectMapper, TableUtils.TableDataJsonPrettyPrinter.INSTANCE, System.out);
			return 0;
		} catch (Exception e) {
			System.err.println("Error viewing datum delete job: %s".formatted(e.getMessage()));
		}
		return 1;
	}

	/**
	 * View a datum delete task.
	 * 
	 * @param restClient   the REST client
	 * @param objectMapper the object mapper
	 * @param jobId        the job ID to view
	 * @return the updated job info
	 * @throws IllegalStateException if an error occurs
	 */
	public static DatumDeleteTaskInfo viewDatumDeleteTask(RestClient restClient, ObjectMapper objectMapper,
			String jobId) {
		// @formatter:off
		final JsonNode response = checkSuccess(restClient.get()
				.uri(b -> {
					b.path("/solaruser/api/v1/sec/user/expire/datum-delete/jobs/{jobId}");
					return b.build(jobId);
				})
			.accept(MediaType.APPLICATION_JSON)
			.retrieve()
			.body(JsonNode.class)
			);		
		// @formatter:on

		try {
			return objectMapper.treeToValue(response.path("data"), DatumDeleteTaskInfo.class).normalized();
		} catch (JsonProcessingException | IllegalArgumentException e) {
			throw new IllegalStateException("Error parsing datum delete job response: " + e.getMessage(), e);
		}
	}

	/**
	 * Get datum delete job tabular structure columns.
	 * 
	 * @return the columns
	 * @see #tableDataRow(DatumDeleteTaskInfo)
	 */
	public static Column[] tableDataColumns() {
		// @formatter:off
		return new Column[] {
				new Column().header("Job ID").dataAlign(LEFT),
				new Column().header("Submit Date").dataAlign(LEFT),
				new Column().header("State").dataAlign(LEFT),
				new Column().header("Success").dataAlign(LEFT),
				new Column().header("Started At").dataAlign(LEFT),
				new Column().header("Completed At").dataAlign(LEFT),
				new Column().header("Deleted").dataAlign(RIGHT),
				new Column().header("% Complete").dataAlign(RIGHT),
				new Column().header("Delete Criteria").dataAlign(LEFT),
			};
		// @formatter:on
	}

	/**
	 * Convert a datum delete job into a tabular structure.
	 * 
	 * @param info the configuration to convert
	 * @return the row data
	 * @see #tableDataColumns()
	 */
	public static Object[] tableDataRow(DatumDeleteTaskInfo info) {
		// @formatter:off
		return new Object[] {
				info.jobId(),
				info.submitDate(),
				info.jobState(),
				onlyTrueValue(info.success()),
				nonEpochInstant(info.startedDate()),
				nonEpochInstant(info.completionDate()),
				info.resultCount(),
				"%.0f".formatted(info.percentComplete() * 100.0),
				TableUtils.basicTable(info.filterConfiguration(), null, null, false),
			};
		// @formatter:on
	}

}
