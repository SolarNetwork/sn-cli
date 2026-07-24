package s10k.tool.c2c.ds.rake.cmd;

import static com.github.freva.asciitable.HorizontalAlign.LEFT;
import static com.github.freva.asciitable.HorizontalAlign.RIGHT;
import static s10k.tool.common.util.RestUtils.checkSuccess;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.freva.asciitable.Column;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import s10k.tool.c2c.domain.CloudDatumStreamRakeTaskConfiguration;
import s10k.tool.c2c.domain.CloudIntegrationsFilter;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.ClaimableJobState;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.util.RestUtils;
import s10k.tool.common.util.TableUtils;

/**
 * List Cloud Datum Stream Rake Task configurations.
 */
@Component("listRakeTasksCmd")
@Command(name = "list", sortSynopsis = false, showDefaultValues = true)
public class ListTasksCmd extends BaseSubCmd<RakeTasksCmd> implements Callable<Integer> {

	// @formatter:off
	@Option(names = { "-stream", "--stream-id" },
			description = "a datum stream ID to include tasks for",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "datumStreamId")
	Long[] datumStreamIds;

	@Option(names = { "-task", "--task-id" },
			description = "a rake task ID to include",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "rakeTaskId")
	Long[] taskIds;

	@Option(names = { "-source", "--source-id" },
			description = "a source ID pattern to match",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "sourceId")
	String[] sourceIds;
	
	@Option(names = { "-node", "--node-id" },
			description = "a node ID to match (on datum stream's objectId property)",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "nodeId")
	Long[] nodeIds;

	@Option(names = { "-state", "--job-state" },
			description = "a job state to match",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "jobState")
	ClaimableJobState jobStates[];

	@Option(names = {"-M", "--max"},
			description = "return at most this many results", paramLabel = "max")
	int maxResults;

	@Option(names = {"-O", "--offset"},
			description = "start returning results from this offset, 0 being the first result")
	long resultOffset;

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
	public ListTasksCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		final RestClient restClient = restClient();
		final CloudIntegrationsFilter filter = filter();

		try {
			final List<CloudDatumStreamRakeTaskConfiguration> tasks = listCloudDatumStreamRakeTasks(restClient,
					objectMapper, filter);

			final List<?> tableData = (displayMode == ResultDisplayMode.JSON ? tasks
					: tasks.stream().map(c -> tableDataRow(c)).toList());
			TableUtils.renderTableData(tableDataColumns(), tableData, displayMode, objectMapper,
					TableUtils.TableDataJsonPrettyPrinter.INSTANCE, System.out);
			return 0;
		} catch (Exception e) {
			System.err.println("Error listing cloud datum stream rake tasks: %s".formatted(e.getMessage()));
		}
		return 1;
	}

	private CloudIntegrationsFilter filter() {
		final CloudIntegrationsFilter filter = new CloudIntegrationsFilter();
		if (datumStreamIds != null && datumStreamIds.length > 0) {
			filter.setDatumStreamIds(List.of(datumStreamIds));
		}
		if (taskIds != null && taskIds.length > 0) {
			filter.setTaskIds(List.of(taskIds));
		}
		if (jobStates != null && jobStates.length > 0) {
			filter.setClaimableJobStates(List.of(jobStates));
		}
		if (nodeIds != null && nodeIds.length > 0) {
			filter.setNodeIds(List.of(nodeIds));
		}
		if (sourceIds != null && sourceIds.length > 0) {
			filter.setSourceIds(List.of(sourceIds));
		}
		if (maxResults > 0) {
			filter.setMax(maxResults);
		}
		if (resultOffset > 0) {
			filter.setOffset(resultOffset);
		}
		return filter;
	}

	/**
	 * Get integrations info tabular structure columns.
	 * 
	 * @return the columns
	 */
	public static Column[] tableDataColumns() {
		// @formatter:off
		return new Column[] {
				new Column().header("Task ID").dataAlign(RIGHT),
				new Column().header("Stream ID").dataAlign(RIGHT),
				new Column().header("State").dataAlign(LEFT),
				new Column().header("Execute At").dataAlign(LEFT),
				new Column().header("Offset").dataAlign(LEFT),
				new Column().header("Message").dataAlign(LEFT),
			};
		// @formatter:on
	}

	/**
	 * Convert poll task listing into a tabular structure.
	 * 
	 * @param conf the configuration to convert
	 * @return the metadata data
	 */
	public static Object[] tableDataRow(CloudDatumStreamRakeTaskConfiguration conf) {
		// @formatter:off
		return new Object[] {
				conf.configId(),
				conf.datumStreamId(),
				conf.state(),
				conf.executeAt(),
				conf.offset(),
				rakeTaskMessage(conf),
			};
		// @formatter:on
	}

	public static String rakeTaskMessage(CloudDatumStreamRakeTaskConfiguration conf) {
		String result = conf.message();

		final Map<String, Object> serviceProps = conf.serviceProperties();
		final Object msg = (serviceProps != null ? serviceProps.get("message") : null);
		if (msg != null) {
			if (result == null || result.isEmpty()) {
				result = msg.toString();
			} else {
				result += " " + msg;
			}
		}
		return result;
	}

	/**
	 * View a cloud datum stream mapping properties.
	 * 
	 * @param restClient   the REST client
	 * @param objectMapper the object mapper
	 * @param filter       an optional filter
	 * @return the result
	 */
	public static List<CloudDatumStreamRakeTaskConfiguration> listCloudDatumStreamRakeTasks(RestClient restClient,
			ObjectMapper objectMapper, CloudIntegrationsFilter filter) {
		// @formatter:off
		JsonNode response = restClient.get()
			.uri(b -> {
				b.path("/solaruser/api/v1/sec/user/c2c/datum-stream-rake-tasks");
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

		List<CloudDatumStreamRakeTaskConfiguration> result = new ArrayList<>(response.path("data").size());
		for (JsonNode node : response.path("data").path("results")) {
			CloudDatumStreamRakeTaskConfiguration conf;
			try {
				conf = objectMapper.treeToValue(node, CloudDatumStreamRakeTaskConfiguration.class);
			} catch (JsonProcessingException | IllegalArgumentException e) {
				throw new IllegalStateException(
						"Error parsing cloud datum stream rake task list response: " + e.getMessage(), e);
			}
			if (conf != null) {
				result.add(conf);
			}
		}
		return result;
	}

}
