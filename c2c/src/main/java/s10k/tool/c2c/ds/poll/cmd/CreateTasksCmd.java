package s10k.tool.c2c.ds.poll.cmd;

import static com.github.freva.asciitable.HorizontalAlign.LEFT;
import static com.github.freva.asciitable.HorizontalAlign.RIGHT;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static s10k.tool.c2c.ds.poll.cmd.ListTasksCmd.listCloudDatumStreamPollTasks;
import static s10k.tool.c2c.util.CloudIntegrationRestUtils.datumStreamsOfType;
import static s10k.tool.c2c.util.CloudIntegrationsUtils.datumStreamServiceLocalizedName;
import static s10k.tool.common.util.RestUtils.checkSuccess;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Callable;

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
import s10k.tool.c2c.domain.CloudDatumStreamPollTaskConfiguration;
import s10k.tool.c2c.domain.CloudIntegrationsFilter;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.ClaimableJobState;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.util.DateUtils;
import s10k.tool.common.util.TableUtils;

/**
 * Create Cloud Datum Stream Poll Task configurations.
 */
@Component("createPollTasksCmd")
@Command(name = "create", sortSynopsis = false)
public class CreateTasksCmd extends BaseSubCmd<PollTasksCmd> implements Callable<Integer> {

	// @formatter:off
	@Option(names = { "-stream", "--stream-id" },
			description = "a datum stream ID to create tasks for",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "datumStreamId")
	Long[] datumStreamIds;

	@Option(names = { "-t", "--stream-type" },
			description = """
					a datum stream type to restrict creating tasks for;
					can be prefixed with ! to exclude that type""",			
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "datumStreamType")
	String[] types;
	
	@Option(names = { "-s", "--start-date" },
			description = "the date to start polling data from")
	LocalDateTime startDate;

	@Option(names = { "-tz", "--time-zone" },
			description = "a time zone to interpret the startDate date at, instead of the local time zone")
	ZoneId zone;

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
			final Instant start = DateUtils
					.zonedDate(startDate != null ? startDate
							: LocalDateTime.now(zone != null ? zone : ZoneId.systemDefault()), zone)
					.toInstant().truncatedTo(ChronoUnit.MINUTES);

			// get datum streams
			final SortedMap<Long, CloudDatumStreamConfiguration> datumStreams = datumStreamsOfType(restClient,
					objectMapper, filter, types);

			// get existing poll tasks
			final SortedMap<Long, CloudDatumStreamPollTaskConfiguration> tasks = pollTasks(restClient, filter);

			// figure out which streams need a task created
			final SortedMap<Long, CloudDatumStreamPollTaskConfiguration> tasksToCreate = tasksToCreate(
					datumStreams.values(), tasks, start);

			if (tasksToCreate.isEmpty()) {
				System.err.println("No tasks need to be created.");
				return 0;
			}

			if (!isDryRun()) {
				createTasks(restClient, tasksToCreate.values());
			}

			final List<?> tableData = (displayMode == ResultDisplayMode.JSON ? tasksToCreate.values().stream().toList()
					: tasksToCreate.entrySet().stream()
							.map(e -> tableDataRow(datumStreams.get(e.getKey()), e.getValue())).toList());
			TableUtils.renderTableData(tableDataColumns(), tableData, displayMode, objectMapper,
					TableUtils.TableDataJsonPrettyPrinter.INSTANCE, System.out);
			return 0;
		} catch (

		Exception e) {
			System.err.println("Error create cloud datum stream poll tasks: %s".formatted(e.getMessage()));
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

	private SortedMap<Long, CloudDatumStreamPollTaskConfiguration> pollTasks(RestClient restClient,
			CloudIntegrationsFilter filter) {
		return listCloudDatumStreamPollTasks(restClient, objectMapper, filter).stream().collect(
				toMap(CloudDatumStreamPollTaskConfiguration::datumStreamId, identity(), (_, n) -> n, TreeMap::new));
	}

	private SortedMap<Long, CloudDatumStreamPollTaskConfiguration> tasksToCreate(
			Collection<CloudDatumStreamConfiguration> datumStreams,
			SortedMap<Long, CloudDatumStreamPollTaskConfiguration> tasks, Instant startAt) {
		var result = new TreeMap<Long, CloudDatumStreamPollTaskConfiguration>();
		for (CloudDatumStreamConfiguration datumStream : datumStreams) {
			if (!tasks.containsKey(datumStream.configId())) {
				result.put(datumStream.configId(), new CloudDatumStreamPollTaskConfiguration(
				// @formatter:off
						  datumStream.configId()
						, ClaimableJobState.Queued.keyValue()
						, startAt
						, startAt
						, null
						, null
						// @formatter:on
				));
			}
		}
		return result;
	}

	private static Column[] tableDataColumns() {
		// @formatter:off
		return new Column[] {
				new Column().header("Datum Stream ID").dataAlign(RIGHT),
				new Column().header("Datum Stream Type").dataAlign(LEFT),
				new Column().header("Object ID").dataAlign(RIGHT),
				new Column().header("Source ID").dataAlign(LEFT),
				new Column().header("State").dataAlign(LEFT),
				new Column().header("Start At").dataAlign(LEFT),
			};
		// @formatter:on
	}

	private static Object[] tableDataRow(CloudDatumStreamConfiguration datumStream,
			CloudDatumStreamPollTaskConfiguration conf) {
		// @formatter:off
		return new Object[] {
				datumStream.configId(),
				datumStreamServiceLocalizedName(datumStream.serviceIdentifier()),
				datumStream.objectId(),
				datumStream.sourceIdsValue(),
				conf.state(),
				conf.startAt(),
			};
		// @formatter:on
	}

	private void createTasks(RestClient restClient, Collection<CloudDatumStreamPollTaskConfiguration> tasksToCreate) {
		for (CloudDatumStreamPollTaskConfiguration task : tasksToCreate) {
			createPollTask(restClient, task.datumStreamId(), task);
		}
	}

	private static void createPollTask(RestClient restClient, Long datumStreamId,
			CloudDatumStreamPollTaskConfiguration task) {
		// @formatter:off
			JsonNode response = restClient.post()
				.uri(b -> {
					b.path("/solaruser/api/v1/sec/user/c2c/datum-stream-poll-tasks/{datumStreamId}");
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

}
