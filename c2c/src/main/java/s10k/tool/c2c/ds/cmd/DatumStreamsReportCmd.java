package s10k.tool.c2c.ds.cmd;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toMap;

import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.function.Function;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;

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

/**
 * Identify Cloud Datum Stream potential issues.
 */
@Component
@Command(name = "report", sortSynopsis = false)
public class DatumStreamsReportCmd extends BaseSubCmd<DatumStreamsCmd> implements Callable<Integer> {

	public static final Duration DEFAULT_LAG_THRESHOLD = Duration.ofDays(3);

	// @formatter:off
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

	@Override
	public Integer call() throws Exception {
		final RestClient restClient = restClient();
		final Instant now = Instant.now();
		final Instant lagMax = now.minus(lagThreshold);

		try {
			final SortedMap<Long, CloudDatumStreamConfiguration> allDatumStreams = allDatumStreams(restClient);
			final SortedMap<Long, CloudDatumStreamPollTaskConfiguration> allPollTasks = allPollTasks(restClient);
			final SortedMap<Long, SortedMap<Period, CloudDatumStreamRakeTaskConfiguration>> allRakeTasks = allRakeTasks(
					restClient);

			final var pollCheckup = new PollTaskCheckup();
			final var rakeCheckup = new RakeTaskCheckup();

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
							rakeCheckup.errorTasks
									.computeIfAbsent(task.datumStreamId(),
											_ -> new TreeMap<>(CloudIntegrationsUtils::comparePeriods))
									.put(offset, task);
						}
					}
					// find lagging tasks
					if (task.executeAt().isBefore(lagMax)) {
						rakeCheckup.laggingTasks.computeIfAbsent(task.datumStreamId(),
								_ -> new TreeMap<>(CloudIntegrationsUtils::comparePeriods)).put(offset, task);
					}
				}
			}

			final var checkup = new Checkup(allDatumStreams.size(), pollCheckup, rakeCheckup).normalized();

			// if (displayMode == JSON) {
			OutputUtils.writeJsonObject(objectMapper, checkup);
			// }
			return 0;
		} catch (Exception e) {
			System.err.println("Error viewing cloud datum streams: %s".formatted(e.getMessage()));
		}
		return 1;

	}

	private record Checkup(int datumStreamCount, PollTaskCheckup pollTasks, RakeTaskCheckup rakeTasks) {

		private Checkup normalized() {
			return new Checkup(datumStreamCount,
					pollTasks != null && !pollTasks.isEmpty() ? pollTasks.normalized() : null,
					rakeTasks != null && !rakeTasks.isEmpty() ? rakeTasks.normalized() : null);
		}

	}

	private record PollTaskCheckup(SortedMap<Long, CloudDatumStreamConfiguration> datumStreamsWithoutTasks,
			SortedMap<Long, CloudDatumStreamPollTaskConfiguration> stoppedTasks,
			SortedMap<Long, CloudDatumStreamPollTaskConfiguration> errorTasks,
			SortedMap<Long, CloudDatumStreamPollTaskConfiguration> laggingTasks) {

		private PollTaskCheckup() {
			this(new TreeMap<>(), new TreeMap<>(), new TreeMap<>(), new TreeMap<>());
		}

		private PollTaskCheckup normalized() {
			return new PollTaskCheckup(nonEmpty(datumStreamsWithoutTasks), nonEmpty(stoppedTasks), nonEmpty(errorTasks),
					nonEmpty(laggingTasks));
		}

		private boolean isEmpty() {
			// @formatter:off
			return (datumStreamsWithoutTasks == null || datumStreamsWithoutTasks.isEmpty())
					&& (stoppedTasks == null || stoppedTasks.isEmpty())
					&& (errorTasks == null || errorTasks.isEmpty())
					&& (laggingTasks == null || laggingTasks.isEmpty());
			// @formatter:on
		}

	}

	private record RakeTaskCheckup(SortedMap<Long, CloudDatumStreamConfiguration> datumStreamsWithoutTasks,
			SortedMap<Long, SortedMap<Period, CloudDatumStreamRakeTaskConfiguration>> stoppedTasks,
			SortedMap<Long, SortedMap<Period, CloudDatumStreamRakeTaskConfiguration>> errorTasks,
			SortedMap<Long, SortedMap<Period, CloudDatumStreamRakeTaskConfiguration>> laggingTasks) {

		private RakeTaskCheckup() {
			this(new TreeMap<>(), new TreeMap<>(), new TreeMap<>(), new TreeMap<>());
		}

		private RakeTaskCheckup normalized() {
			return new RakeTaskCheckup(nonEmpty(datumStreamsWithoutTasks), nonEmpty(stoppedTasks), nonEmpty(errorTasks),
					nonEmpty(laggingTasks));
		}

		private boolean isEmpty() {
			// @formatter:off
			return (datumStreamsWithoutTasks == null || datumStreamsWithoutTasks.isEmpty())
					&& (stoppedTasks == null || stoppedTasks.isEmpty())
					&& (errorTasks == null || errorTasks.isEmpty())
					&& (laggingTasks == null || laggingTasks.isEmpty());
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
