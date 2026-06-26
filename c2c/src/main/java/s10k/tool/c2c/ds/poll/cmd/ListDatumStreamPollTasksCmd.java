package s10k.tool.c2c.ds.poll.cmd;

import static com.github.freva.asciitable.HorizontalAlign.LEFT;
import static com.github.freva.asciitable.HorizontalAlign.RIGHT;
import static org.springframework.util.StringUtils.arrayToCommaDelimitedString;
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
import s10k.tool.c2c.domain.CloudDatumStreamPollTaskConfiguration;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.util.TableUtils;

/**
 * List Cloud Datum Stream Poll Task configurations.
 */
@Component
@Command(name = "list", sortSynopsis = false)
public class ListDatumStreamPollTasksCmd extends BaseSubCmd<PollTasksCmd> implements Callable<Integer> {

	// @formatter:off
	@Option(names = { "-stream", "--stream-id" },
			description = "a datum stream ID to include tasks for",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "nodeId")
	Long[] datumStreamIds;

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
	public ListDatumStreamPollTasksCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		final RestClient restClient = restClient();

		try {
			final List<CloudDatumStreamPollTaskConfiguration> tasks = listCloudDatumStreamPollTasks(restClient,
					objectMapper, datumStreamIds);

			final List<?> tableData = (displayMode == ResultDisplayMode.JSON ? tasks
					: tasks.stream().map(c -> tableDataRow(c)).toList());
			TableUtils.renderTableData(tableDataColumns(), tableData, displayMode, objectMapper,
					TableUtils.TableDataJsonPrettyPrinter.INSTANCE, System.out);
			return 0;
		} catch (Exception e) {
			System.err.println("Error listing cloud datum stream poll tasks: %s".formatted(e.getMessage()));
		}
		return 1;
	}

	/**
	 * Get integrations info tabular structure columns.
	 * 
	 * @return the columns
	 */
	public static Column[] tableDataColumns() {
		// @formatter:off
		return new Column[] {
				new Column().header("Datum Stream ID").dataAlign(RIGHT),
				new Column().header("State").dataAlign(LEFT),
				new Column().header("Execute At").dataAlign(LEFT),
				new Column().header("Start At").dataAlign(LEFT),
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
	public static Object[] tableDataRow(CloudDatumStreamPollTaskConfiguration conf) {
		// @formatter:off
		return new Object[] {
				conf.datumStreamId(),
				conf.state(),
				conf.executeAt(),
				conf.startAt(),
				pollTaskMessage(conf),
			};
		// @formatter:on
	}

	public static String pollTaskMessage(CloudDatumStreamPollTaskConfiguration conf) {
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
	 * @param restClient     the REST client
	 * @param objectMapper   the object mapper
	 * @param datumStreamIds the IDs of datum streams to view poll tasks for
	 * @return the result
	 */
	public static List<CloudDatumStreamPollTaskConfiguration> listCloudDatumStreamPollTasks(RestClient restClient,
			ObjectMapper objectMapper, Long[] datumStreamIds) {
		// @formatter:off
		JsonNode response = restClient.get()
			.uri(b -> {
				b.path("/solaruser/api/v1/sec/user/c2c/datum-stream-poll-tasks");
				if (datumStreamIds != null && datumStreamIds.length > 0 ) {
					b.queryParam("datumStreamIds", arrayToCommaDelimitedString(datumStreamIds));
				}
				return b.build();
			})
			.accept(MediaType.APPLICATION_JSON)
			.retrieve()
			.body(JsonNode.class)
			;		
		// @formatter:on

		checkSuccess(response);

		List<CloudDatumStreamPollTaskConfiguration> result = new ArrayList<>(response.path("data").size());
		for (JsonNode node : response.path("data").path("results")) {
			CloudDatumStreamPollTaskConfiguration conf;
			try {
				conf = objectMapper.treeToValue(node, CloudDatumStreamPollTaskConfiguration.class);
			} catch (JsonProcessingException | IllegalArgumentException e) {
				throw new IllegalStateException(
						"Error parsing cloud datum stream poll task list response: " + e.getMessage(), e);
			}
			if (conf != null) {
				result.add(conf);
			}
		}
		return result;
	}

}
