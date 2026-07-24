package s10k.tool.datum.imp.cmd;

import static s10k.tool.datum.imp.util.DatumImportRestUtils.listDatumImportTasks;

import java.util.List;
import java.util.concurrent.Callable;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.util.TableUtils;
import s10k.tool.datum.imp.domain.DatumImportState;
import s10k.tool.datum.imp.domain.DatumImportTaskInfo;
import s10k.tool.datum.imp.domain.DatumImportsFilter;

/**
 * List datum import jobs.
 */
@Command(name = "list", sortSynopsis = false, showDefaultValues = true, descriptionHeading = "%n", optionListHeading = "%n", description = {
		"List the details of datum import jobs matching search criteria.%n" })
public class ListImportJobsCmd extends BaseSubCmd<DatumImportsCmd> implements Callable<Integer> {

	// @formatter:off
	@Option(names = { "-state", "--job-state" },
			description = "a job state to match",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "jobState")
	DatumImportState[] jobStates;

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
	public ListImportJobsCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		final RestClient restClient = restClient();
		final DatumImportsFilter filter = filter();
		try {
			final List<DatumImportTaskInfo> tasks = listDatumImportTasks(restClient, objectMapper, filter);

			final List<?> tableData = (displayMode == ResultDisplayMode.JSON ? tasks
					: tasks.stream().map(c -> ViewImportJobCmd.tableDataRow(c)).toList());
			TableUtils.renderTableData(ViewImportJobCmd.tableDataColumns(), tableData, displayMode, objectMapper,
					TableUtils.TableDataJsonPrettyPrinter.INSTANCE, System.out);
			return 0;
		} catch (Exception e) {
			System.err.println("Error listing datum: %s".formatted(e.getMessage()));
		}
		return 1;
	}

	private DatumImportsFilter filter() {
		final DatumImportsFilter filter = new DatumImportsFilter();
		if (jobStates != null && jobStates.length > 0) {
			filter.setJobStates(List.of(jobStates));
		}
		return filter;
	}

}
