package s10k.tool.nodes.cmd;

import static com.github.freva.asciitable.HorizontalAlign.LEFT;
import static com.github.freva.asciitable.HorizontalAlign.RIGHT;
import static s10k.tool.common.util.RestUtils.checkSuccess;

import java.util.ArrayList;
import java.util.List;
import java.util.SequencedCollection;
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
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.util.TableUtils;
import s10k.tool.nodes.domain.NodeInfo;

/**
 * List nodes.
 */
@Component
@Command(name = "list")
public class ListNodesCmd extends BaseSubCmd<NodesCmd> implements Callable<Integer> {

	// @formatter:off
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
	public ListNodesCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		final RestClient restClient = restClient();
		try {
			SequencedCollection<NodeInfo> nodes = listNodes(restClient, objectMapper);
			if (nodes.isEmpty()) {
				System.err.println("No nodes available.");
				return 1;
			}
			List<Object[]> tableData = nodes.stream().map(ListNodesCmd::nodeInfoRow).toList();
			TableUtils.renderTableData(nodeInfoColumns(), tableData, displayMode, objectMapper,
					TableUtils.TableDataJsonPrettyPrinter.INSTANCE, System.out);
			return 0;
		} catch (Exception e) {
			System.err.println("Error listing node metadata: %s".formatted(e.getMessage()));
		}
		return 1;
	}

	/**
	 * Get node info tabular structure columns.
	 * 
	 * @return the columns
	 */
	public static Column[] nodeInfoColumns() {
		// @formatter:off
		return new Column[] {
				new Column().header("Node ID").dataAlign(RIGHT),
				new Column().header("Created").dataAlign(LEFT),
				new Column().header("Public").dataAlign(LEFT),
				new Column().header("Country").dataAlign(LEFT),
				new Column().header("Time Zone").dataAlign(LEFT),
			};
		// @formatter:on
	}

	/**
	 * Convert node info into a tabular structure.
	 * 
	 * @param info the node info to convert
	 * @return the tabular data
	 */
	public static Object[] nodeInfoRow(NodeInfo info) {
		// @formatter:off
		return new Object[] {
				info.nodeId(),
				info.created(),
				!info.requiresAuthorization(),
				info.location() != null ? info.location().getCountry() : null,
				info.location() != null ? info.location().getTimeZoneId() : null,
			};
		// @formatter:on
	}

	/**
	 * List nodes.
	 * 
	 * @param restClient   the REST client
	 * @param objectMapper the mapper to use
	 * @return the nodes
	 */
	public static SequencedCollection<NodeInfo> listNodes(RestClient restClient, ObjectMapper objectMapper) {
		// @formatter:off
		JsonNode response = restClient.get()
				.uri(b -> {
					b.path("/solaruser/api/v1/sec/nodes");
					return b.build();
				})
			.accept(MediaType.APPLICATION_JSON)
			.retrieve()
			.body(JsonNode.class)
			;		
		// @formatter:on

		checkSuccess(response);

		SequencedCollection<NodeInfo> result = new ArrayList<>(response.path("data").path("results").size());
		for (JsonNode node : response.path("data").path("results")) {
			try {
				NodeInfo info = objectMapper.treeToValue(node, NodeInfo.class);
				if (info != null) {
					result.add(info);
				}
			} catch (JsonProcessingException e) {
				// ignore and move on
			}
		}
		return result;
	}

}
