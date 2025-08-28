package s10k.tool.nodes.meta.cmd;

import static net.solarnetwork.util.StringUtils.commaDelimitedStringFromCollection;
import static s10k.tool.common.util.RestUtils.checkSuccess;
import static s10k.tool.common.util.TableUtils.basicTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
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
import com.fasterxml.jackson.databind.ObjectWriter;

import net.solarnetwork.domain.datum.GeneralDatumMetadata;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.nodes.domain.NodeMetadata;

/**
 * List node metadata.
 */
@Component
@Command(name = "list")
public class ListNodeMetadataCmd extends BaseSubCmd<NodeMetadataCmd> implements Callable<Integer> {

	@Option(names = { "-node",
			"--node-id" }, description = "a node ID to return metadata for", split = "\\s*,\\s*", splitSynopsisLabel = ",", paramLabel = "nodeId", required = true)
	Long[] nodeIds;

	@Option(names = { "-filter", "--filter" }, description = "a metadata filter")
	String filter;

	/**
	 * Constructor.
	 * 
	 * @param reqFactory   the HTTP request factory to use
	 * @param objectMapper the mapper to use
	 */
	public ListNodeMetadataCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		final RestClient restClient = restClient();

		ObjectWriter pretty = objectMapper.writerWithDefaultPrettyPrinter();

		try {
			Collection<NodeMetadata> metas = listNodeMetadata(restClient, objectMapper, nodeIds, filter);
			boolean multi = false;
			for (NodeMetadata meta : metas) {
				if (multi) {
					System.out.println("");
				} else {
					multi = true;
				}
				Map<String, Object> tableData = new LinkedHashMap<String, Object>(4);
				tableData.put("nodeId", meta.nodeId());
				if (meta.created() != null) {
					tableData.put("created", meta.created());
				}
				if (meta.updated() != null) {
					tableData.put("updated", meta.updated());
				}

				GeneralDatumMetadata gdm = meta.metadata();

				System.out.print(basicTable(tableData, "Property", "Value", false));
				if (gdm != null) {
					System.out.println(pretty.writeValueAsString(gdm));
				}
			}
		} catch (Exception e) {
			System.err.println("Error listing node metadata: %s".formatted(e.getMessage()));
		}
		return 1;
	}

	/**
	 * List node metadata matching a search filter.
	 * 
	 * @param restClient   the REST client
	 * @param objectMapper the object mapper
	 * @param nodeIds      the node IDs
	 * @param filter       the optional filter
	 * @return the instruction statuses
	 * @throws IllegalStateException if the instruction listing is not available
	 */
	private Collection<NodeMetadata> listNodeMetadata(RestClient restClient, ObjectMapper objectMapper, Long[] nodeIds,
			String filter) {
		assert filter != null;
		// @formatter:off
		JsonNode response = restClient.get()
			.uri(b -> {
				b.path("/solaruser/api/v1/sec/nodes/meta");
				if (nodeIds != null && nodeIds.length > 0) {
					b.queryParam("nodeIds", commaDelimitedStringFromCollection(Arrays.asList(nodeIds)));
				}
				if (filter != null && !filter.isBlank()) {
					b.queryParam("metadataFilter", filter);
				}
				return b.build();
			})
			.accept(MediaType.APPLICATION_JSON)
			.retrieve()
			.body(JsonNode.class)
			;		
		// @formatter:on

		checkSuccess(response);

		List<NodeMetadata> result = new ArrayList<>(response.path("data").path("results").size());
		for (JsonNode node : response.path("data").path("results")) {
			NodeMetadata meta;
			try {
				meta = objectMapper.treeToValue(node, NodeMetadata.class);
			} catch (JsonProcessingException | IllegalArgumentException e) {
				log.warn("Error parsing metadata response: {}", e.toString(), e);
				continue;
			}
			if (meta != null) {
				result.add(meta);
			}
		}
		return result;
	}

}
