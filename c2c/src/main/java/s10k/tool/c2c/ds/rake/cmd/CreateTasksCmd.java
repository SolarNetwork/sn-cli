package s10k.tool.c2c.ds.rake.cmd;

import static com.github.freva.asciitable.HorizontalAlign.LEFT;
import static com.github.freva.asciitable.HorizontalAlign.RIGHT;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toMap;
import static org.springframework.util.StreamUtils.nonClosing;
import static s10k.tool.c2c.ds.rake.cmd.ListTasksCmd.listCloudDatumStreamRakeTasks;
import static s10k.tool.c2c.util.CloudIntegrationRestUtils.datumStreamsOfType;
import static s10k.tool.c2c.util.CloudIntegrationsUtils.datumStreamServiceLocalizedName;
import static s10k.tool.common.util.RestUtils.checkSuccess;

import java.io.IOException;
import java.io.OutputStream;
import java.time.Instant;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SequencedCollection;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.function.Function;

import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.freva.asciitable.Column;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import s10k.tool.c2c.domain.CloudDatumStreamConfiguration;
import s10k.tool.c2c.domain.CloudDatumStreamRakeTaskConfiguration;
import s10k.tool.c2c.domain.CloudIntegrationsFilter;
import s10k.tool.c2c.util.CloudIntegrationsUtils;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.ClaimableJobState;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.util.OutputUtils;
import s10k.tool.common.util.TableUtils;
import s10k.tool.nodes.cmd.ListNodesCmd;
import s10k.tool.nodes.domain.NodeInfo;

/**
 * Create Cloud Datum Stream Rake Task configurations.
 */
@Component("createRakeTasksCmd")
@Command(name = "create", sortSynopsis = false)
public class CreateTasksCmd extends BaseSubCmd<RakeTasksCmd> implements Callable<Integer> {

	// @formatter:off
	@Option(names = { "-stream", "--stream-id" },
			description = "a datum stream ID to create tasks for",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "datumStreamId")
	Long[] datumStreamIds;

	@Option(names = { "-o", "--offset" },
			description = "the rake task offset",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "offset",
			defaultValue = "P3D,P7D,P14D,P21D")
	Period[] offsets;

	@Option(names = { "-t", "--stream-type" },
			description = """
					a datum stream type to restrict creating tasks for;
					can be prefixed with ! to exclude that type""",			
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "datumStreamType")
	String[] types;
	
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
	public CreateTasksCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		final RestClient restClient = restClient();
		try {
			final CloudIntegrationsFilter filter = filter();

			// get datum streams
			final SortedMap<Long, CloudDatumStreamConfiguration> datumStreams = datumStreamsOfType(restClient,
					objectMapper, filter, types);

			// get existing rake tasks
			final SortedMap<Long, SortedMap<Period, CloudDatumStreamRakeTaskConfiguration>> tasks = rakeTasks(
					restClient, filter);

			final SortedMap<Long, TaskActions> taskActions = taskActions(datumStreams, tasks);

			final Map<Long, ZoneId> nodeTimeZones = nodeTimeZonesForCreate(restClient, taskActions.values());

			if (!isDryRun()) {
				updateTasks(restClient, datumStreams, nodeTimeZones, taskActions);
			}
			if (displayMode == ResultDisplayMode.JSON) {
				OutputUtils.writeJsonObject(objectMapper, taskActions);
			} else {
				generateTaskActionReport(datumStreams, nodeTimeZones, taskActions);
			}
			return 0;
		} catch (Exception e) {
			System.err.println("Error create cloud datum stream rake tasks: %s".formatted(e.getMessage()));
		}
		return 1;
	}

	private CloudIntegrationsFilter filter() {
		final CloudIntegrationsFilter filter = new CloudIntegrationsFilter();
		if (datumStreamIds != null && datumStreamIds.length > 0) {
			filter.setDatumStreamIds(List.of(datumStreamIds));
		}
		return filter;
	}

	private SortedMap<Long, SortedMap<Period, CloudDatumStreamRakeTaskConfiguration>> rakeTasks(RestClient restClient,
			CloudIntegrationsFilter filter) {
		return listCloudDatumStreamRakeTasks(restClient, objectMapper, filter).stream()
				.collect(groupingBy(CloudDatumStreamRakeTaskConfiguration::datumStreamId, TreeMap::new,
						mapping(Function.identity(), toMap(t -> Period.parse(t.offset()), identity(), (_, n) -> n,
								() -> new TreeMap<>(CloudIntegrationsUtils::comparePeriods)))));
	}

	public record TaskActions(Long datumStreamId, SortedSet<Period> missingOffsets,
			SortedMap<Period, CloudDatumStreamRakeTaskConfiguration> undesiredOffsets) {

	}

	/**
	 * Generate a mapping of task actions that need to be performed: creating
	 * missing tasks or removing undesired ones.
	 * 
	 * @param datumStreams  the set of datum streams tasks are being created for
	 * @param existingTasks any existing tasks for the datum streams
	 * @return a set of actions that need performing, per datum stream
	 */
	private SortedMap<Long, TaskActions> taskActions(SortedMap<Long, CloudDatumStreamConfiguration> datumStreams,
			SortedMap<Long, SortedMap<Period, CloudDatumStreamRakeTaskConfiguration>> existingTasks) {
		final SortedMap<Long, TaskActions> result = new TreeMap<>();
		final SortedSet<Period> desiredOffsets = new TreeSet<>(CloudIntegrationsUtils::comparePeriods);
		desiredOffsets.addAll(List.of(offsets));
		for (CloudDatumStreamConfiguration datumStream : datumStreams.values()) {
			SortedMap<Period, CloudDatumStreamRakeTaskConfiguration> existingOffsets = new TreeMap<>(
					CloudIntegrationsUtils::comparePeriods);
			if (existingTasks.containsKey(datumStream.configId())) {
				existingOffsets.putAll(existingTasks.get(datumStream.configId()));
			}
			if (existingOffsets.keySet().equals(desiredOffsets)) {
				continue;
			}
			SortedSet<Period> missingOffsets = new TreeSet<>(CloudIntegrationsUtils::comparePeriods);
			for (Period p : desiredOffsets) {
				if (existingOffsets.remove(p) == null) {
					missingOffsets.add(p);
				}
			}
			result.put(datumStream.configId(),
					new TaskActions(datumStream.configId(), missingOffsets, existingOffsets));
		}
		return result;
	}

	public static Column[] tableDataColumns() {
		// @formatter:off
		return new Column[] {
				new Column().header("Datum Stream ID").dataAlign(RIGHT),
				new Column().header("Datum Stream Type").dataAlign(LEFT),
				new Column().header("Object ID").dataAlign(RIGHT),
				new Column().header("Source ID").dataAlign(LEFT),
				new Column().header("Offset").dataAlign(LEFT),
				new Column().header("Execute At").dataAlign(LEFT),
				new Column().header("Action").dataAlign(LEFT),
			};
		// @formatter:on
	}

	public static Object[] tableDataRow(CloudDatumStreamConfiguration datumStream,
			CloudDatumStreamRakeTaskConfiguration conf, String action) {
		// @formatter:off
		return new Object[] {
				conf.datumStreamId(),
				datumStreamServiceLocalizedName(datumStream.serviceIdentifier()),
				datumStream.objectId(),
				datumStream.sourceIdsValue(),
				conf.offset(),
				conf.executeAt(),
				action,
			};
		// @formatter:on
	}

	private Map<Long, ZoneId> nodeTimeZonesForCreate(RestClient restClient, Collection<TaskActions> actionsList) {
		if (actionsList == null || actionsList.isEmpty()) {
			return Map.of();
		}
		Map<Long, ZoneId> result = new HashMap<>();
		boolean needNodeInfos = false;
		for (TaskActions actions : actionsList) {
			if (!actions.missingOffsets.isEmpty()) {
				needNodeInfos = true;
				break;
			}
		}
		if (needNodeInfos) {
			SequencedCollection<NodeInfo> infos = ListNodesCmd.listNodes(restClient, objectMapper);
			if (infos != null) {
				for (NodeInfo info : infos) {
					result.put(info.nodeId(), ZoneId.of(info.timeZoneId()));
				}
			}
		}
		return result;
	}

	private void updateTasks(RestClient restClient, SortedMap<Long, CloudDatumStreamConfiguration> datumStreams,
			Map<Long, ZoneId> nodeTimeZones, SortedMap<Long, TaskActions> taskActions) {
		for (TaskActions actions : taskActions.values()) {
			final CloudDatumStreamConfiguration datumStream = datumStreams.get(actions.datumStreamId());
			final ZoneId zone = (nodeTimeZones.containsKey(datumStream.objectId())
					? nodeTimeZones.get(datumStream.objectId())
					: ZoneOffset.UTC);
			if (actions.missingOffsets.size() == offsets.length) {
				bulkCreateRakeTasks(restClient, actions.datumStreamId, zone, actions.missingOffsets);
			} else {
				for (Period offset : actions.missingOffsets) {
					createRakeTask(restClient, actions.datumStreamId, zone, offset);
				}
				for (CloudDatumStreamRakeTaskConfiguration task : actions.undesiredOffsets.values()) {
					deleteRakeTask(restClient, task.configId());
				}
			}
		}
	}

	private static void bulkCreateRakeTasks(RestClient restClient, Long datumStreamId, ZoneId zone,
			Set<Period> offsets) {
		final Instant executeAt = ZonedDateTime.now(zone).toLocalDate().plusDays(1).atStartOfDay(zone).toInstant();
		final List<CloudDatumStreamRakeTaskConfiguration> tasks = offsets.stream().map(p -> {
			return new CloudDatumStreamRakeTaskConfiguration(
			// @formatter:off
					  null
					, null
					, ClaimableJobState.Queued.keyValue()
					, executeAt
					, p.toString()
					, null
					, null
					// @formatter:on
			);
		}).toList();

		// @formatter:off
		JsonNode response = restClient.post()
			.uri(b -> {
				b.path("/solaruser/api/v1/sec/user/c2c/datum-stream-rake-tasks/{datumStreamId}/tasks");
				return b.build(datumStreamId);
			})
			.contentType(MediaType.APPLICATION_JSON)
			.body(tasks)
			.accept(MediaType.APPLICATION_JSON)
			.retrieve()
			.body(JsonNode.class)
			;		
		// @formatter:on

		checkSuccess(response);
	}

	private static void createRakeTask(RestClient restClient, Long datumStreamId, ZoneId zone, Period offset) {
		final Instant executeAt = ZonedDateTime.now(zone).toLocalDate().plusDays(1).atStartOfDay(zone).toInstant();
		final CloudDatumStreamRakeTaskConfiguration task = new CloudDatumStreamRakeTaskConfiguration(
		// @formatter:off
					  null
					, datumStreamId
					, ClaimableJobState.Queued.keyValue()
					, executeAt
					, offset.toString()
					, null
					, null
					// @formatter:on
		);

		// @formatter:off
		JsonNode response = restClient.post()
			.uri(b -> {
				b.path("/solaruser/api/v1/sec/user/c2c/datum-stream-rake-tasks");
				return b.build(datumStreamId);
			})
			.contentType(MediaType.APPLICATION_JSON)
			.body(task)
			.accept(MediaType.APPLICATION_JSON)
			.retrieve()
			.body(JsonNode.class)
			;		
		// @formatter:on

		checkSuccess(response);
	}

	private static void deleteRakeTask(RestClient restClient, Long taskId) {
		// @formatter:off
		JsonNode response = restClient.delete()
			.uri(b -> {
				b.path("/solaruser/api/v1/sec/user/c2c/datum-stream-rake-tasks/{taskId}");
				return b.build(taskId);
			})
			.accept(MediaType.APPLICATION_JSON)
			.retrieve()
			.body(JsonNode.class)
			;		
		// @formatter:on

		checkSuccess(response);
	}

	@SuppressWarnings("ClosingStandardOutputStreams")
	private void generateTaskActionReport(SortedMap<Long, CloudDatumStreamConfiguration> datumStreams,
			Map<Long, ZoneId> nodeTimeZones, SortedMap<Long, TaskActions> taskActions) throws IOException {
		if (taskActions == null || taskActions.isEmpty()) {
			return;
		}
		try (OutputStream out = nonClosing(System.out)) {
			TableUtils.renderTableData(tableDataColumns(), taskActions.values().stream().flatMap(actions -> {
				List<Object[]> rows = new ArrayList<>();
				rows.addAll(actions.missingOffsets.stream().map(p -> {
					final CloudDatumStreamConfiguration datumStream = datumStreams.get(actions.datumStreamId());
					final ZoneId tz = (nodeTimeZones.containsKey(datumStream.objectId())
							? nodeTimeZones.get(datumStream.objectId())
							: ZoneOffset.UTC);
					final Instant executeAt = ZonedDateTime.now(tz).toLocalDate().plusDays(1).atStartOfDay(tz)
							.toInstant();
					return tableDataRow(datumStream, new CloudDatumStreamRakeTaskConfiguration(
					// @formatter:off
									  null
									, actions.datumStreamId()
									, ClaimableJobState.Queued.keyValue()
									, executeAt
									, p.toString()
									, null
									, null
									// @formatter:on
					), "Create");
				}).toList());
				rows.addAll(actions.undesiredOffsets.values().stream()
						.map(c -> tableDataRow(datumStreams.get(actions.datumStreamId()), c, "Remove")).toList());
				return rows.stream();
			}).toList(), displayMode, objectMapper, TableUtils.TableDataJsonPrettyPrinter.INSTANCE, out);
		}
	}

}
