/**
 * 
 */
package s10k.tool.nodes.meta.cmd;

import static s10k.tool.common.util.RestUtils.checkSuccess;

import java.util.concurrent.Callable;

import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import s10k.tool.common.cmd.BaseSubCmd;

/**
 * Delete node metadata.
 */
@Component
@Command(name = "delete")
public class DeleteNodeMetadataCmd extends BaseSubCmd<NodeMetadataCmd> implements Callable<Integer> {

	@Option(names = { "-node", "--node-id" }, description = "a node ID to delete metadata from", required = true)
	Long nodeId;

	/**
	 * Constructor.
	 * 
	 * @param reqFactory   the HTTP request factory to use
	 * @param objectMapper the mapper to use
	 */
	public DeleteNodeMetadataCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		final RestClient restClient = restClient();

		try {
			deleteMetadata(restClient, nodeId);
			System.out.println("Node metadata deleted.");
			return 0;
		} catch (Exception e) {
			System.err.println("Error deleting node metadata: %s".formatted(e.getMessage()));
		}
		return 1;
	}

	/**
	 * Execute an instruction given a request map.
	 * 
	 * @param restClient the REST client
	 * @param nodeId     the ID of the node to delete metadata from
	 * @throws IllegalStateException if the metadata fails to delete
	 */
	private static void deleteMetadata(RestClient restClient, Long nodeId) {
		assert nodeId != null;

		// @formatter:off
		JsonNode response = restClient.delete()
			.uri("/solaruser/api/v1/sec/nodes/meta/" +nodeId)
			.accept(MediaType.APPLICATION_JSON)
			.retrieve()
			.body(JsonNode.class)
			;		
		// @formatter:on

		checkSuccess(response);
	}

}
