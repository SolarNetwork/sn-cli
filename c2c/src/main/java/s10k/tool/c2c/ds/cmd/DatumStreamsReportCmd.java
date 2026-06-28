package s10k.tool.c2c.ds.cmd;

import static com.github.freva.asciitable.HorizontalAlign.LEFT;
import static com.github.freva.asciitable.HorizontalAlign.RIGHT;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toMap;
import static org.springframework.util.StreamUtils.nonClosing;
import static s10k.tool.c2c.ds.poll.cmd.ListDatumStreamPollTasksCmd.pollTaskMessage;
import static s10k.tool.c2c.ds.rake.cmd.ListDatumStreamRakeTasksCmd.rakeTaskMessage;
import static s10k.tool.c2c.util.CloudIntegrationsUtils.datumStreamServiceLocalizedName;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.function.Function;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.freva.asciitable.Column;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import s10k.tool.c2c.domain.CloudDatumStreamConfiguration;
import s10k.tool.c2c.domain.CloudDatumStreamPollTaskConfiguration;
import s10k.tool.c2c.domain.CloudDatumStreamRakeTaskConfiguration;
import s10k.tool.c2c.ds.poll.cmd.ListDatumStreamPollTasksCmd;
import s10k.tool.c2c.ds.rake.cmd.ListDatumStreamRakeTasksCmd;
import s10k.tool.c2c.util.CloudIntegrationsUtils;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.ClaimableJobState;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.util.OutputUtils;
import s10k.tool.common.util.TableUtils;

/**
 * Identify Cloud Datum Stream potential issues.
 */
@Component
@Command(name = "report", sortSynopsis = false)
public class DatumStreamsReportCmd extends BaseSubCmd<DatumStreamsCmd> implements Callable<Integer> {

	public static final Duration DEFAULT_LAG_THRESHOLD = Duration.ofDays(3);

	// @formatter:off
	@Option(names = { "-d", "--directory" },
			description = "a directory to export the report to")
	String outputDirectory;

	@Option(names = { "-lag", "--lag-threshold" },
			description = "how to display the data",
			defaultValue = "P3D")
	Duration lagThreshold = DEFAULT_LAG_THRESHOLD;

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
	public DatumStreamsReportCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@SuppressWarnings("ClosingStandardOutputStreams")
	@Override
	public Integer call() throws Exception {
		final RestClient restClient = restClient();

		try {
			final var checkup = createCheckup(restClient);
			if (checkup.isWithoutWarnings()) {
				System.err.println("No warnings found.");
				return 0;
			}

			final Path outputDir = (outputDirectory != null ? OutputUtils.ensureDirectory(outputDirectory) : null);

			if (displayMode == ResultDisplayMode.JSON) {
				if (outputDir == null) {
					OutputUtils.writeJsonObject(objectMapper, checkup.asReport());
				} else {
					// export to file
					try (OutputStream out = Files.newOutputStream(outputDir.resolve("datum-stream-report.json"))) {
						objectMapper.writeValue(out, checkup.asReport());
					}
				}
			} else {
				// overall report
				try (OutputStream out = (outputDir != null
						? Files.newOutputStream(outputDir.resolve(fileName("datum-stream-overall-report")))
						: nonClosing(System.out))) {
					// @formatter:off
					TableUtils.renderTableData(new Column[] {
						  new Column().header("Datum Stream Count").dataAlign(RIGHT)
						, new Column().header("Warning Count").dataAlign(RIGHT)
						, new Column().header("Poll Task Count").dataAlign(RIGHT)
						, new Column().header("Poll Warning Count").dataAlign(RIGHT)
						, new Column().header("Rake Task Count").dataAlign(RIGHT)
						, new Column().header("Rake Warning Count").dataAlign(RIGHT)
					}, List.of(List.of(
						  checkup.datumStreamCount()
						, checkup.warningCount()
						, checkup.pollTasks.taskCount()
						, checkup.pollTasks.warningCount()
						, checkup.rakeTasks.taskCount()
						, checkup.rakeTasks.warningCount()
					)), displayMode, objectMapper,
							TableUtils.TableDataJsonPrettyPrinter.INSTANCE, out);
					// @formatter:on
				}

				// poll task reports
				generatePollTaskReport(checkup.datumStreams, checkup.pollTasks.stoppedTasks, "Stopped",
						"datum-stream-poll-task-stopped-report", outputDir);
				generatePollTaskReport(checkup.datumStreams, checkup.pollTasks.errorTasks, "Failing",
						"datum-stream-poll-task-failing-report", outputDir);
				generatePollTaskReport(checkup.datumStreams, checkup.pollTasks.laggingTasks, "Lagging",
						"datum-stream-poll-task-lagging-report", outputDir);

				// rake task reports
				generateRakeTaskReport(checkup.datumStreams, checkup.rakeTasks.stoppedTasks, "Stopped",
						"datum-stream-rake-task-stopped-report", outputDir);
				generateRakeTaskReport(checkup.datumStreams, checkup.rakeTasks.errorTasks, "Failing",
						"datum-stream-rake-task-failing-report", outputDir);
				generateRakeTaskReport(checkup.datumStreams, checkup.rakeTasks.laggingTasks, "Lagging",
						"datum-stream-rake-task-lagging-report", outputDir);
			}
			if (outputDir != null) {
				System.err.printf("Report generated in %s\n", outputDir);
			}
			return 0;
		} catch (Exception e) {
			System.err.printf("Error viewing cloud datum streams: %s\n", e.getMessage());
		}
		return 1;

	}

	@SuppressWarnings("ClosingStandardOutputStreams")
	private void generatePollTaskReport(SortedMap<Long, CloudDatumStreamConfiguration> datumStreams,
			SortedMap<Long, CloudDatumStreamPollTaskConfiguration> tasks, String title, String fileName, Path outputDir)
			throws IOException {
		if (tasks != null && !tasks.isEmpty()) {
			if (outputDir == null) {
				System.out.printf("\n\nPoll Tasks %s:\n", title);
			}
			try (OutputStream out = (outputDir != null ? Files.newOutputStream(outputDir.resolve(fileName(fileName)))
					: nonClosing(System.out))) {
				TableUtils.renderTableData(PollTaskCheckup.tableDataColumns(), tasks.values().stream()
						.map(c -> PollTaskCheckup.tableDataRow(datumStreams.get(c.datumStreamId()), c)).toList(),
						displayMode, objectMapper, TableUtils.TableDataJsonPrettyPrinter.INSTANCE, out);
			}
		}
	}

	@SuppressWarnings("ClosingStandardOutputStreams")
	private void generateRakeTaskReport(SortedMap<Long, CloudDatumStreamConfiguration> datumStreams,
			SortedMap<Long, SortedMap<Period, CloudDatumStreamRakeTaskConfiguration>> tasks, String title,
			String fileName, Path outputDir) throws IOException {
		if (tasks != null && !tasks.isEmpty()) {
			if (outputDir == null) {
				System.out.printf("\n\nRake Tasks %s:\n", title);
			}
			try (OutputStream out = (outputDir != null ? Files.newOutputStream(outputDir.resolve(fileName(fileName)))
					: nonClosing(System.out))) {
				TableUtils.renderTableData(RakeTaskCheckup.tableDataColumns(),
						tasks.values().stream().flatMap(m -> m.values().stream())
								.map(c -> RakeTaskCheckup.tableDataRow(datumStreams.get(c.datumStreamId()), c))
								.toList(),
						displayMode, objectMapper, TableUtils.TableDataJsonPrettyPrinter.INSTANCE, out);
			}
		}
	}

	private String fileName(String name) {
		return "%s.%s".formatted(name, displayMode == ResultDisplayMode.CSV ? "csv" : "txt");
	}

	private Checkup createCheckup(RestClient restClient) {
		final SortedMap<Long, CloudDatumStreamConfiguration> allDatumStreams = allDatumStreams(restClient);
		final SortedMap<Long, CloudDatumStreamPollTaskConfiguration> allPollTasks = allPollTasks(restClient);
		final SortedMap<Long, SortedMap<Period, CloudDatumStreamRakeTaskConfiguration>> allRakeTasks = allRakeTasks(
				restClient);

		final Instant now = Instant.now();
		final Instant lagMax = now.minus(lagThreshold);

		final var pollCheckup = new PollTaskCheckup(allPollTasks);
		final var rakeCheckup = new RakeTaskCheckup(allRakeTasks);

		for (CloudDatumStreamConfiguration ds : allDatumStreams.values()) {
			// find streams without any poll task
			if (!allPollTasks.containsKey(ds.configId())) {
				pollCheckup.datumStreamsWithoutTasks.put(ds.configId(), ds);
			}

			// find streams without any rake task
			if (!allRakeTasks.containsKey(ds.configId())) {
				rakeCheckup.datumStreamsWithoutTasks.put(ds.configId(), ds);
			}
		}

		// poll task checks
		for (CloudDatumStreamPollTaskConfiguration task : allPollTasks.values()) {
			// find Completed tasks
			if (ClaimableJobState.Completed.keyValue().equals(task.state())) {
				pollCheckup.stoppedTasks.put(task.datumStreamId(), task);
			} else {
				// look for error tasks
				var sprops = task.serviceProperties();
				if (sprops != null && sprops.get("errorCount") instanceof Number n && n.longValue() > 0L) {
					pollCheckup.errorTasks.put(task.datumStreamId(), task);
				}
			}
			// find lagging tasks
			if (task.startAt().isBefore(lagMax)) {
				pollCheckup.laggingTasks.put(task.datumStreamId(), task);
			}
		}

		// rake task checks
		for (SortedMap<Period, CloudDatumStreamRakeTaskConfiguration> taskMap : allRakeTasks.values()) {
			for (Entry<Period, CloudDatumStreamRakeTaskConfiguration> taskEntry : taskMap.entrySet()) {
				var offset = taskEntry.getKey();
				var task = taskEntry.getValue();
				// find Completed tasks
				if (ClaimableJobState.Completed.keyValue().equals(task.state())) {
					rakeCheckup.stoppedTasks.computeIfAbsent(task.datumStreamId(),
							_ -> new TreeMap<>(CloudIntegrationsUtils::comparePeriods)).put(offset, task);
				} else {
					// look for error tasks
					var sprops = task.serviceProperties();
					if (sprops != null && sprops.get("errorCount") instanceof Number n && n.longValue() > 0L) {
						rakeCheckup.errorTasks.computeIfAbsent(task.datumStreamId(),
								_ -> new TreeMap<>(CloudIntegrationsUtils::comparePeriods)).put(offset, task);
					}
				}
				// find lagging tasks
				if (task.executeAt().isBefore(lagMax)) {
					rakeCheckup.laggingTasks.computeIfAbsent(task.datumStreamId(),
							_ -> new TreeMap<>(CloudIntegrationsUtils::comparePeriods)).put(offset, task);
				}
			}
		}

		return new Checkup(allDatumStreams, pollCheckup, rakeCheckup);
	}

	@JsonPropertyOrder({ "datumStreamCount", "warningCount", "datumStreamsWithWarnings", "pollTasks", "rakeTasks" })
	@JsonIgnoreProperties("datumStreams")
	@RegisterReflectionForBinding
	public record Checkup(SortedMap<Long, CloudDatumStreamConfiguration> datumStreams, PollTaskCheckup pollTasks,
			RakeTaskCheckup rakeTasks) {

		@JsonGetter
		public int datumStreamCount() {
			return (datumStreams != null ? datumStreams.size() : 0);
		}

		@JsonGetter
		public int warningCount() {
			return (pollTasks != null ? pollTasks.warningCount() : 0)
					+ (rakeTasks != null ? rakeTasks.warningCount() : 0);
		}

		@JsonGetter
		public SortedMap<Long, CloudDatumStreamConfiguration> datumStreamsWithWarnings() {
			return datumStreams.values().stream()
					.filter(ds -> pollTasks.datumStreamHasWarning(ds.configId())
							|| rakeTasks.datumStreamHasWarning(ds.configId()))
					.collect(toMap(ds -> ds.configId(), identity(), (_, n) -> n, TreeMap::new));
		}

		private Checkup asReport() {
			// @formatter:off
			return new Checkup(nonEmpty(datumStreams),
					pollTasks != null ? pollTasks.asReport() : null,
					rakeTasks != null ? rakeTasks.asReport() : null);
			// @formatter:on
		}

		private boolean isWithoutWarnings() {
			// @formatter:off
			return (datumStreams == null || datumStreams.isEmpty())
					&& (pollTasks == null || pollTasks.isWithoutWarnings())
					&& (rakeTasks == null || rakeTasks.isWithoutWarnings());
			// @formatter:on
		}

	}

	@JsonPropertyOrder({ "taskCount", "warningCount", "datumStreamsWithoutTasks", "stoppedTasks", "errorTasks",
			"laggingTasks" })
	@JsonIgnoreProperties("tasks")
	@RegisterReflectionForBinding
	public record PollTaskCheckup(SortedMap<Long, CloudDatumStreamPollTaskConfiguration> tasks,
			SortedMap<Long, CloudDatumStreamConfiguration> datumStreamsWithoutTasks,
			SortedMap<Long, CloudDatumStreamPollTaskConfiguration> stoppedTasks,
			SortedMap<Long, CloudDatumStreamPollTaskConfiguration> errorTasks,
			SortedMap<Long, CloudDatumStreamPollTaskConfiguration> laggingTasks) {

		private PollTaskCheckup(SortedMap<Long, CloudDatumStreamPollTaskConfiguration> tasks) {
			this(tasks, new TreeMap<>(), new TreeMap<>(), new TreeMap<>(), new TreeMap<>());
		}

		private PollTaskCheckup asReport() {
			return new PollTaskCheckup(nonEmpty(tasks), nonEmpty(datumStreamsWithoutTasks), nonEmpty(stoppedTasks),
					nonEmpty(errorTasks), nonEmpty(laggingTasks));
		}

		@JsonGetter
		public int taskCount() {
			return (tasks != null ? tasks.size() : 0);
		}

		@JsonGetter
		public int warningCount() {
			// @formatter:off
			return (datumStreamsWithoutTasks != null ? datumStreamsWithoutTasks.size() : 0)
					+ (stoppedTasks != null ? stoppedTasks.size() : 0)
					+ (errorTasks != null ? errorTasks.size() : 0)
					+ (laggingTasks != null ? laggingTasks.size() : 0);
			// @formatter:on
		}

		private boolean isWithoutWarnings() {
			return warningCount() < 1;
		}

		private boolean datumStreamHasWarning(Long datumStreamId) {
			// @formatter:off
			return (datumStreamsWithoutTasks != null && datumStreamsWithoutTasks.containsKey(datumStreamId))
					|| (stoppedTasks != null && stoppedTasks.containsKey(datumStreamId))
					|| (errorTasks != null && errorTasks.containsKey(datumStreamId))
					|| (laggingTasks != null && laggingTasks.containsKey(datumStreamId))
					;
			// @formatter:on
		}

		public static Column[] tableDataColumns() {
			// @formatter:off
			return new Column[] {
					new Column().header("Datum Stream ID").dataAlign(RIGHT),
					new Column().header("Datum Stream Type").dataAlign(LEFT),
					new Column().header("State").dataAlign(LEFT),
					new Column().header("Error Count").dataAlign(RIGHT),
					new Column().header("State").dataAlign(LEFT),
					new Column().header("Start At").dataAlign(LEFT),
					new Column().header("Message").dataAlign(LEFT),
				};
			// @formatter:on
		}

		public static Object[] tableDataRow(CloudDatumStreamConfiguration datumStream,
				CloudDatumStreamPollTaskConfiguration conf) {
			// @formatter:off
			return new Object[] {
					conf.datumStreamId(),
					datumStreamServiceLocalizedName(datumStream.serviceIdentifier()),
					conf.state(),
					conf.errorCount(),
					conf.executeAt(),
					conf.startAt(),
					pollTaskMessage(conf),
				};
			// @formatter:on
		}
	}

	@JsonPropertyOrder({ "taskCount", "warningCount", "datumStreamsWithoutTasks", "stoppedTasks", "errorTasks",
			"laggingTasks" })
	@JsonIgnoreProperties("tasks")
	@RegisterReflectionForBinding
	public record RakeTaskCheckup(SortedMap<Long, SortedMap<Period, CloudDatumStreamRakeTaskConfiguration>> tasks,
			SortedMap<Long, CloudDatumStreamConfiguration> datumStreamsWithoutTasks,
			SortedMap<Long, SortedMap<Period, CloudDatumStreamRakeTaskConfiguration>> stoppedTasks,
			SortedMap<Long, SortedMap<Period, CloudDatumStreamRakeTaskConfiguration>> errorTasks,
			SortedMap<Long, SortedMap<Period, CloudDatumStreamRakeTaskConfiguration>> laggingTasks) {

		private RakeTaskCheckup(SortedMap<Long, SortedMap<Period, CloudDatumStreamRakeTaskConfiguration>> tasks) {
			this(tasks, new TreeMap<>(), new TreeMap<>(), new TreeMap<>(), new TreeMap<>());
		}

		private RakeTaskCheckup asReport() {
			return new RakeTaskCheckup(nonEmpty(tasks), nonEmpty(datumStreamsWithoutTasks), nonEmpty(stoppedTasks),
					nonEmpty(errorTasks), nonEmpty(laggingTasks));
		}

		@JsonGetter
		public int taskCount() {
			return (tasks != null ? tasks.size() : 0);
		}

		@JsonGetter
		public int warningCount() {
			// @formatter:off
			return (datumStreamsWithoutTasks != null ? datumStreamsWithoutTasks.size() : 0)
					+ (stoppedTasks != null ? stoppedTasks.size() : 0)
					+ (errorTasks != null ? errorTasks.size() : 0)
					+ (laggingTasks != null ? laggingTasks.size() : 0);
			// @formatter:on
		}

		private boolean isWithoutWarnings() {
			return warningCount() < 1;
		}

		private boolean datumStreamHasWarning(Long datumStreamId) {
			// @formatter:off
			return (datumStreamsWithoutTasks != null && datumStreamsWithoutTasks.containsKey(datumStreamId))
					|| (stoppedTasks != null && stoppedTasks.containsKey(datumStreamId))
					|| (errorTasks != null && errorTasks.containsKey(datumStreamId))
					|| (laggingTasks != null && laggingTasks.containsKey(datumStreamId))
					;
			// @formatter:on
		}

		public static Column[] tableDataColumns() {
			// @formatter:off
			return new Column[] {
					new Column().header("Datum Stream ID").dataAlign(RIGHT),
					new Column().header("Datum Stream Type").dataAlign(LEFT),
					new Column().header("Task ID").dataAlign(RIGHT),
					new Column().header("State").dataAlign(LEFT),
					new Column().header("Error Count").dataAlign(RIGHT),
					new Column().header("Execute At").dataAlign(LEFT),
					new Column().header("Offset").dataAlign(LEFT),
					new Column().header("Message").dataAlign(LEFT),
				};
			// @formatter:on
		}

		public static Object[] tableDataRow(CloudDatumStreamConfiguration datumStream,
				CloudDatumStreamRakeTaskConfiguration conf) {
			// @formatter:off
			return new Object[] {
					conf.datumStreamId(),
					datumStreamServiceLocalizedName(datumStream.serviceIdentifier()),
					conf.configId(),
					conf.state(),
					conf.errorCount(),
					conf.executeAt(),
					conf.offset(),
					rakeTaskMessage(conf),
				};
			// @formatter:on
		}
	}

	private static <K, V> SortedMap<K, V> nonEmpty(SortedMap<K, V> map) {
		return (map == null || map.isEmpty() ? null : map);
	}

	private SortedMap<Long, CloudDatumStreamConfiguration> allDatumStreams(RestClient restClient) {
		return ListDatumStreamsCmd.listCloudDatumStreams(restClient, objectMapper).stream()
				.collect(toMap(CloudDatumStreamConfiguration::configId, identity(), (_, n) -> n, TreeMap::new));
	}

	private SortedMap<Long, CloudDatumStreamPollTaskConfiguration> allPollTasks(RestClient restClient) {
		return ListDatumStreamPollTasksCmd.listCloudDatumStreamPollTasks(restClient, objectMapper, null).stream()
				.collect(toMap(CloudDatumStreamPollTaskConfiguration::datumStreamId, identity(), (_, n) -> n,
						TreeMap::new));
	}

	private SortedMap<Long, SortedMap<Period, CloudDatumStreamRakeTaskConfiguration>> allRakeTasks(
			RestClient restClient) {
		return ListDatumStreamRakeTasksCmd.listCloudDatumStreamRakeTasks(restClient, objectMapper, null).stream()
				.collect(groupingBy(CloudDatumStreamRakeTaskConfiguration::datumStreamId, TreeMap::new,
						mapping(Function.identity(), toMap(t -> Period.parse(t.offset()), identity(), (_, n) -> n,
								() -> new TreeMap<>(CloudIntegrationsUtils::comparePeriods)))));
	}
}
