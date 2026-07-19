package s10k.tool.c2c.ds.rake.cmd;

import static com.github.freva.asciitable.HorizontalAlign.LEFT;
import static com.github.freva.asciitable.HorizontalAlign.RIGHT;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static s10k.tool.c2c.ds.rake.cmd.ListTasksCmd.listCloudDatumStreamRakeTasks;
import static s10k.tool.c2c.util.CloudIntegrationRestUtils.listCloudDatumStreams;
import static s10k.tool.c2c.util.CloudIntegrationsUtils.datumStreamServiceLocalizedName;
import static s10k.tool.common.util.RestUtils.checkSuccess;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
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
import picocli.CommandLine.Parameters;
import s10k.tool.c2c.domain.CloudDatumStreamConfiguration;
import s10k.tool.c2c.domain.CloudDatumStreamRakeTaskConfiguration;
import s10k.tool.c2c.domain.CloudIntegrationsFilter;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.ClaimableJobState;
import s10k.tool.common.domain.EnabledOrDisabled;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.util.TableUtils;

/**
 * Change the runtime state of pake tasks.
 */
@Component("changeRakeTasksStateCmd")
@Command(name = "change-state", sortSynopsis = false)
public class ChangeStateCmd extends BaseSubCmd<RakeTasksCmd> implements Callable<Integer> {

	// @formatter:off
		@Option(names = { "-stream", "--stream-id" },
				description = "a datum stream ID of the task to change the state of",
				split = "\\s*,\\s*",
				splitSynopsisLabel = ",",
				paramLabel = "datumStreamId",
				required = true)
		Long[] datumStreamIds;

		@Option(names = { "-mode", "--display-mode" },
				description = "how to display the data",
				defaultValue = "PRETTY")
		ResultDisplayMode displayMode = ResultDisplayMode.PRETTY;
		
		@Parameters(arity = "1")
		EnabledOrDisabled desiredState;
		// @formatter:on

	/**
	 * Constructor.
	 * 
	 * @param reqFactory   the HTTP request factory to use
	 * @param objectMapper the mapper to use
	 */
	public ChangeStateCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		final RestClient restClient = restClient();

		final var filter = new CloudIntegrationsFilter();
		filter.setDatumStreamIds(List.of(datumStreamIds));

		try {
			final List<CloudDatumStreamRakeTaskConfiguration> tasks = listCloudDatumStreamRakeTasks(restClient,
					objectMapper, filter).stream()
					.sorted(Comparator.comparing(CloudDatumStreamRakeTaskConfiguration::datumStreamId)
							.thenComparing(CloudDatumStreamRakeTaskConfiguration::configId))
					.toList();
			final Map<Long, CloudDatumStreamConfiguration> streams = (displayMode != ResultDisplayMode.JSON
					? listCloudDatumStreams(restClient, objectMapper, filter).stream()
							.collect(toMap(c -> c.configId(), identity()))
					: null);

			if (!isDryRun()) {
				for (CloudDatumStreamRakeTaskConfiguration task : tasks) {
					changeTaskState(restClient, task.configId(), desiredState);
				}
			}

			final List<?> tableData = (displayMode == ResultDisplayMode.JSON
					? (isDryRun() ? tasks.stream().map(c -> c.copyWithState(desiredState.asJobState())).toList()
							: tasks)
					: tasks.stream().map(c -> stateChangeTableDataRow(c, desiredState, streams.get(c.datumStreamId())))
							.toList());
			TableUtils.renderTableData(stateChangeTableDataColumns(), tableData, displayMode, objectMapper,
					TableUtils.TableDataJsonPrettyPrinter.INSTANCE, System.out);
			return 0;
		} catch (Exception e) {
			System.err.println("Error changing cloud datum stream pake task state: %s".formatted(e.getMessage()));
		}

		return 1;
	}

	private static void changeTaskState(RestClient restClient, Long taskId, EnabledOrDisabled desiredState) {
		// @formatter:off
			JsonNode response = restClient.post()
				.uri(b -> {
					b.path("/solaruser/api/v1/sec/user/c2c/datum-stream-rake-tasks/{taskId}/state");
					return b.build(taskId);
				})
				.contentType(MediaType.APPLICATION_JSON)
				.body(Map.of("state", desiredState.asJobState().keyValue()))
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.body(JsonNode.class)
				;		
			// @formatter:on

		checkSuccess(response);
	}

	private static Column[] stateChangeTableDataColumns() {
		// @formatter:off
		return new Column[] {
				new Column().header("Stream ID").dataAlign(RIGHT),
				new Column().header("Task ID").dataAlign(RIGHT),
				new Column().header("Name").dataAlign(LEFT),
				new Column().header("Type").dataAlign(LEFT),
				new Column().header("Kind").dataAlign(LEFT),
				new Column().header("Object ID").dataAlign(RIGHT),
				new Column().header("Source ID").dataAlign(LEFT),
				new Column().header("Schedule").dataAlign(LEFT),
				new Column().header("Execute At").dataAlign(LEFT),
				new Column().header("Offset").dataAlign(LEFT),
				new Column().header("Old State").dataAlign(LEFT),
				new Column().header("New State").dataAlign(LEFT),
			};
		// @formatter:on
	}

	private static Object[] stateChangeTableDataRow(CloudDatumStreamRakeTaskConfiguration conf,
			EnabledOrDisabled desiredState, CloudDatumStreamConfiguration datumStream) {
		// @formatter:off
		return new Object[] {
				conf.datumStreamId(),
				conf.configId(),
				datumStream.name(),
				datumStreamServiceLocalizedName(datumStream.serviceIdentifier()),
				datumStream.kind(),
				datumStream.objectId(),
				datumStream.sourceIdsValue(),
				datumStream.schedule(),
				conf.executeAt(),
				conf.offset(),
				EnabledOrDisabled.valueFor(ClaimableJobState.valueFor(conf.state())),
				desiredState,
			};
		// @formatter:on
	}

}
