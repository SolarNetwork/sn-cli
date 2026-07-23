package s10k.tool.datum.imp.cmd;

import static com.github.freva.asciitable.HorizontalAlign.LEFT;
import static com.github.freva.asciitable.HorizontalAlign.RIGHT;
import static s10k.tool.common.util.DateUtils.nonEpochInstant;
import static s10k.tool.common.util.StringUtils.onlyTrueValue;
import static s10k.tool.datum.util.DatumUtils.datumImportServiceLocalizedName;

import java.util.List;
import java.util.concurrent.Callable;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.freva.asciitable.Column;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.util.TableUtils;
import s10k.tool.datum.imp.domain.DatumImportConfiguration;
import s10k.tool.datum.imp.domain.DatumImportTaskInfo;
import s10k.tool.datum.imp.domain.DatumInputServiceConfiguration;
import s10k.tool.datum.imp.util.DatumImportRestUtils;

/**
 * View datum import job status.
 */
@Command(name = "view", sortSynopsis = false)
public class ViewImportJobCmd extends BaseSubCmd<DatumImportsCmd> implements Callable<Integer> {

	// @formatter:off
	@Option(names = { "-j", "--job-id" },
			description = "the ID of the job to view",
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
	public ViewImportJobCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		final RestClient restClient = restClient();
		try {
			final DatumImportTaskInfo result = DatumImportRestUtils.viewDatumImportTask(restClient, objectMapper,
					jobId);

			List<?> tableData = (displayMode == ResultDisplayMode.JSON ? List.of(result)
					: List.of((Object) tableDataRow(result)));
			TableUtils.renderTableData(tableDataColumns(), tableData, displayMode, objectMapper,
					TableUtils.TableDataJsonPrettyPrinter.INSTANCE, System.out);
			return 0;
		} catch (Exception e) {
			System.err.println("Error listing datum: %s".formatted(e.getMessage()));
		}
		return 1;
	}

	/**
	 * Get datum import job tabular structure columns.
	 * 
	 * @return the columns
	 * @see #tableDataRow(DatumImportTaskInfo)
	 */
	public static Column[] tableDataColumns() {
		// @formatter:off
		return new Column[] {
				new Column().header("Job Name").dataAlign(LEFT),
				new Column().header("Job ID").dataAlign(LEFT),
				new Column().header("Group ID").dataAlign(LEFT),
				new Column().header("Submit Date").dataAlign(LEFT),
				new Column().header("State").dataAlign(LEFT),
				new Column().header("Import Date").dataAlign(LEFT),
				new Column().header("Success").dataAlign(LEFT),
				new Column().header("Started At").dataAlign(LEFT),
				new Column().header("Completed At").dataAlign(LEFT),
				new Column().header("Loaded").dataAlign(RIGHT),
				new Column().header("% Complete").dataAlign(RIGHT),
				new Column().header("Batch Size").dataAlign(LEFT),
				new Column().header("Input Service").dataAlign(LEFT),
				new Column().header("Input Time Zone").dataAlign(LEFT),
				new Column().header("Input Properties").dataAlign(LEFT),
			};
		// @formatter:on
	}

	/**
	 * Convert a datum import job into a tabular structure.
	 * 
	 * @param info the configuration to convert
	 * @return the metadata data
	 * @see #tableDataColumns()
	 */
	public static Object[] tableDataRow(DatumImportTaskInfo info) {
		final DatumImportConfiguration conf = info.configuration();
		final DatumInputServiceConfiguration inputConf = conf.inputConfiguration();
		// @formatter:off
		return new Object[] {
				conf.name(),
				info.jobId(),
				info.groupKey(),
				info.submitDate(),
				info.jobState(),
				info.importDate(),
				onlyTrueValue(info.success()),
				nonEpochInstant(info.startedDate()),
				nonEpochInstant(info.completionDate()),
				info.loadedCount(),
				"%.0f".formatted(info.percentComplete() * 100.0),
				conf.batchSize(),
				datumImportServiceLocalizedName(inputConf.getServiceIdentifier()),
				inputConf.getTimeZoneId(),
				TableUtils.basicTable(inputConf.getServiceProperties(), null, null, false),
			};
		// @formatter:on
	}

}
