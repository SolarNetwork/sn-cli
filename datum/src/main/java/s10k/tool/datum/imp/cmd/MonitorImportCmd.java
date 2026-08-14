package s10k.tool.datum.imp.cmd;

import static s10k.tool.common.util.TableUtils.tableConfig;
import static s10k.tool.datum.imp.util.DatumImportRestUtils.viewDatumImportTask;

import java.util.List;
import java.util.concurrent.Callable;

import org.jspecify.annotations.Nullable;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException.TooManyRequests;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import me.tongfei.progressbar.ProgressBar;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.util.TableUtils;
import s10k.tool.datum.imp.domain.DatumImportState;
import s10k.tool.datum.imp.domain.DatumImportTaskInfo;

/**
 * View datum import job status.
 */
@Command(name = "monitor", sortSynopsis = false, showDefaultValues = true, descriptionHeading = "%n", optionListHeading = "%n", description = {
		"Monitor the status of a datum import job.%n" })
public class MonitorImportCmd extends BaseSubCmd<DatumImportsCmd> implements Callable<Integer> {

	// @formatter:off
	@Option(names = { "-j", "--job-id" },
			description = "the ID of the job to monitor",
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
	public MonitorImportCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		try {
			final RestClient restClient = restClient();
			final ObjectMapper objectMapper = objectMapper();
			final ResultDisplayMode displayMode = displayMode(this.displayMode);

			DatumImportTaskInfo info = viewDatumImportTask(restClient, objectMapper, jobId);

			if (info.isImporting()) {
				// render progress
				try (ProgressBar pb = new ProgressBar("Import", 100)) {
					pb.setExtraMessage(progressMessage(info));
					pb.maxHint(-1);
					try {
						while (info.isImporting()) {
							Thread.sleep(info.jobState() == DatumImportState.Executing ? 1000L : 5000L);
							try {
								info = viewDatumImportTask(restClient, objectMapper, jobId);
							} catch (TooManyRequests e) {
								// ignore and continue
							}
							if (pb.isIndefinite() && info.jobState() != DatumImportState.Queued) {
								pb.setExtraMessage(progressMessage(info));
								pb.maxHint(100);
							}
							pb.stepTo((long) (info.percentComplete() * 100));
							pb.setExtraMessage(progressMessage(info));
						}
						pb.stepTo((long) (info.percentComplete() * 100));
						pb.setExtraMessage(progressMessage(info));
					} catch (InterruptedException e) {
						// ignore and finish
					}
				}
			}

			List<?> tableData = (displayMode == ResultDisplayMode.JSON ? List.of(info)
					: List.of((Object) ViewImportJobCmd.tableDataRow(info)));
			TableUtils.renderTableData(ViewImportJobCmd.tableDataColumns(), tableData,
					tableConfig(this, displayMode, prettyStyle()).asJsonSingleton(), objectMapper,
					TableUtils.TableDataJsonPrettyPrinter.INSTANCE, System.out);
			return 0;
		} catch (Exception e) {
			System.err.println("Error monitoring datum import: %s".formatted(e.getMessage()));
		}
		return 1;
	}

	private static String progressMessage(DatumImportTaskInfo info) {
		return switch (info.jobState()) {
		case Claimed, Executing -> "Importing %d".formatted(info.loadedCount());
		case Queued -> "Queued";
		case Staged -> "Staged";
		case Retracted -> "Retracted";
		case Unknown -> "Unknown";
		case Completed -> info.success() ? "Loaded %d".formatted(info.loadedCount()) : "Error";
		};
	}
}
