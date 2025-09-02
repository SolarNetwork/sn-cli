package s10k.tool.nodes.cmd;

import static s10k.tool.common.util.RestUtils.checkSuccess;

import java.util.LinkedHashSet;
import java.util.SequencedCollection;
import java.util.SequencedSet;
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
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.util.TableUtils;

/**
 * List node IDs.
 */
@Component
@Command(name = "ids")
public class ListNodeIdsCmd extends BaseSubCmd<NodesCmd> implements Callable<Integer> {

	// @formatter:off
	@Option(names = { "-filter", "--filter" }, description = "a metadata filter")
	String filter;

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
	public ListNodeIdsCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		final RestClient restClient = restClient();
		try {
			SequencedCollection<Long> nodeIds = listNodeIds(restClient, filter);
			if (nodeIds.isEmpty()) {
				System.err.println("No node IDs matched your criteria.");
				return 1;
			}
			// @formatter:off
			TableUtils.renderTableData(new Column[] {
					new Column().header("Node ID"),
			}, nodeIds, displayMode, objectMapper, TableUtils.TableDataJsonPrettyPrinter.INSTANCE, System.out);
			// @formatter:on
			return 0;
		} catch (Exception e) {
			System.err.println("Error listing node metadata: %s".formatted(e.getMessage()));
		}
		return 1;
	}

	/**
	 * List node IDs.
	 * 
	 * @param restClient     the REST client
	 * @param metadataFilter the optional metadata filter
	 * @return the node IDs
	 */
	public static SequencedCollection<Long> listNodeIds(RestClient restClient, String metadataFilter) {
		// @formatter:off
		JsonNode response = restClient.get()
				.uri(b -> {
					b.path("/solarquery/api/v1/sec/nodes");
					if(metadataFilter != null ) {
						b.queryParam("metadataFilter", metadataFilter);
					}
					return b.build();
				})
			.accept(MediaType.APPLICATION_JSON)
			.retrieve()
			.body(JsonNode.class)
			;		
		// @formatter:on

		checkSuccess(response);

		SequencedSet<Long> result = new LinkedHashSet<>(response.path("data").size());
		for (JsonNode node : response.path("data")) {
			if (node.isNumber()) {
				result.add(node.longValue());
			}
		}
		return result;
	}

}
