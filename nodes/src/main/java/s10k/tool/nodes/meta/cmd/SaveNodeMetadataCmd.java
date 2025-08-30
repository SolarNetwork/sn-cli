package s10k.tool.nodes.meta.cmd;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.util.FileCopyUtils.copyToString;
import static s10k.tool.common.util.RestUtils.checkSuccess;

import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Callable;

import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.solarnetwork.domain.datum.GeneralDatumMetadata;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.util.SystemUtils;

/**
 * Save node metadata.
 */
@Component
@Command(name = "save")
public class SaveNodeMetadataCmd extends BaseSubCmd<NodeMetadataCmd> implements Callable<Integer> {

	@Option(names = { "-node", "--node-id" }, description = "a node ID to save metadata on", required = true)
	Long nodeId;

	@Option(names = { "-r", "--replace" }, description = "replace all existing metadata, rather than add to/update")
	boolean replace;

	@Parameters(index = "0", paramLabel = "<metadata>", description = "the metadata to set, or @file for file to load", arity = "0..1")
	String value;

	/**
	 * Constructor.
	 * 
	 * @param reqFactory   the HTTP request factory to use
	 * @param objectMapper the mapper to use
	 */
	public SaveNodeMetadataCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		if (value == null || value.isBlank()) {
			// try stdin
			if (!SystemUtils.systemConsoleIsTerminal()) {
				value = copyToString(new InputStreamReader(System.in, UTF_8));
			}
		} else if (value.startsWith("@")) {
			Path metaPath = Paths.get(value.substring(1));
			if (!Files.isReadable(metaPath)) {
				System.err.println("Metadata file [%s] not available.".formatted(metaPath));
				return 1;
			}
			value = copyToString(Files.newBufferedReader(metaPath, UTF_8));
		}

		if (value == null || value.isBlank()) {
			System.err.println("No metadata provided. Pass metadata on the command line or standard input.");
			return 1;
		}

		GeneralDatumMetadata gdm;
		try {
			gdm = objectMapper.readValue(value, GeneralDatumMetadata.class);
		} catch (Exception e) {
			System.err.println("Error saving node metadata: %s".formatted(e.getMessage()));
			return 1;
		}

		final RestClient restClient = restClient();

		try {
			saveMetadata(restClient, nodeId, gdm, replace);
			System.out.println("Node metadata %s:".formatted(replace ? "replaced" : "added"));
			objectMapper.writerWithDefaultPrettyPrinter().writeValue(System.out, gdm);
			System.out.println("");
			return 0;
		} catch (Exception e) {
			System.err.println("Error saving node metadata: %s".formatted(e.getMessage()));
		}
		return 1;
	}

	/**
	 * Execute an instruction given a request map.
	 * 
	 * @param restClient the REST client
	 * @param nodeId     the ID of the node to save metadata to
	 * @param metadata   the metadata to save
	 * @param replace    {@code true} to completely replace any existing metadata,
	 *                   or {@code false} to add/merge the given metadata into any
	 *                   existing metadata
	 * @throws IllegalStateException if the metadata fails to save
	 */
	public static void saveMetadata(RestClient restClient, Long nodeId, GeneralDatumMetadata metadata,
			boolean replace) {
		assert nodeId != null;

		// @formatter:off
		JsonNode response = (replace ? restClient.put() : restClient.post())
			.uri("/solaruser/api/v1/sec/nodes/meta/" +nodeId)
			.contentType(MediaType.APPLICATION_JSON)
			.body(metadata)
			.accept(MediaType.APPLICATION_JSON)
			.retrieve()
			.body(JsonNode.class)
			;		
		// @formatter:on

		checkSuccess(response);
	}

}
