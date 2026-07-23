package s10k.tool.datum.imp.cmd;

import static com.github.freva.asciitable.HorizontalAlign.LEFT;
import static com.github.freva.asciitable.HorizontalAlign.RIGHT;
import static s10k.tool.common.util.DateUtils.nonEpochInstant;
import static s10k.tool.common.util.RestUtils.checkSuccess;
import static s10k.tool.common.util.StringUtils.onlyTrueValue;
import static s10k.tool.datum.util.DatumUtils.datumImportServiceLocalizedName;

import java.util.List;
import java.util.concurrent.Callable;

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
import s10k.tool.common.util.RestUtils;
import s10k.tool.common.util.TableUtils;
import s10k.tool.datum.imp.domain.DatumImportConfiguration;
import s10k.tool.datum.imp.domain.DatumImportState;
import s10k.tool.datum.imp.domain.DatumImportTaskInfo;
import s10k.tool.datum.imp.domain.DatumImportsFilter;
import s10k.tool.datum.imp.domain.DatumInputServiceConfiguration;

/**
 * List datum import jobs.
 */
@Command(name = "list", sortSynopsis = false)
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
					: tasks.stream().map(c -> tableDataRow(c)).toList());
			TableUtils.renderTableData(tableDataColumns(), tableData, displayMode, objectMapper,
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

	/**
	 * List datum import tasks.
	 * 
	 * @param restClient   the REST client
	 * @param objectMapper the object mapper
	 * @param filter       the criteria
	 * @return the list of matching tasks
	 * @throws IllegalStateException if an error occurs
	 */
	public static List<DatumImportTaskInfo> listDatumImportTasks(RestClient restClient, ObjectMapper objectMapper,
			DatumImportsFilter filter) {
		// @formatter:off
		JsonNode response = restClient.get()
				.uri(b -> {
					b.path("/solaruser/api/v1/sec/user/import/jobs");
					if (filter != null ) {
						RestUtils.populateQueryParameters(b, filter::toRequestMap);
					}
					return b.build();
				})
			.accept(MediaType.APPLICATION_JSON)
			.retrieve()
			.body(JsonNode.class)
			;		
		// @formatter:on

		checkSuccess(response);

		try {
			DatumImportTaskInfo[] result = objectMapper.treeToValue(response.path("data"), DatumImportTaskInfo[].class);
			return (result != null ? List.of(result) : List.of());
		} catch (JsonProcessingException | IllegalArgumentException e) {
			throw new IllegalStateException("Error parsing datum import task list response: " + e.getMessage(), e);
		}
	}

	/**
	 * Get integrations info tabular structure columns.
	 * 
	 * @return the columns
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
				new Column().header("% Complete").dataAlign(LEFT),
				new Column().header("Batch Size").dataAlign(LEFT),
				new Column().header("Input Service").dataAlign(LEFT),
				new Column().header("Input Time Zone").dataAlign(LEFT),
				new Column().header("Input Properties").dataAlign(LEFT),
			};
		// @formatter:on
	}

	/**
	 * Convert poll task listing into a tabular structure.
	 * 
	 * @param info the configuration to convert
	 * @return the metadata data
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
				info.percentComplete(),
				conf.batchSize(),
				datumImportServiceLocalizedName(inputConf.getServiceIdentifier()),
				inputConf.getTimeZoneId(),
				TableUtils.basicTable(inputConf.getServiceProperties(), null, null, false),
			};
		// @formatter:on
	}

}
