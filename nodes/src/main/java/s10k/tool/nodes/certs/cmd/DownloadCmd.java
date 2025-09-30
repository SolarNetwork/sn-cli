package s10k.tool.nodes.certs.cmd;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.Callable;

import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import me.tongfei.progressbar.ProgressBar;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import s10k.tool.common.cmd.BaseSubCmd;

/**
 * 
 */
@Component
@Command(name = "download", sortSynopsis = false, header = {
// @formatter:off
		"",
		"""
		Download node certificates.
		""",
		// @formatter:on
})
public class DownloadCmd extends BaseSubCmd<CertificatesCmd> implements Callable<Integer> {

	// @formatter:off
	@Option(names = { "-node", "--node-id" },
			description = "a node ID to download the certificate for",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "nodeId",
			required = true)
	Long[] nodeIds;

	@Option(names = { "-d", "--directory" },
			description = "the directory to save certificates to")
	String outputDirectory;
	// @formatter:on

	/**
	 * Constructor.
	 * 
	 * @param reqFactory   the HTTP request factory to use
	 * @param objectMapper the mapper to use
	 */
	public DownloadCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		if (nodeIds == null || nodeIds.length < 1) {
			System.err.println("No node IDs provided.");
			return 1;
		}
		final Path outputDir = Paths.get(outputDirectory != null ? outputDirectory : ".");
		if (!Files.exists(outputDir)) {
			try {
				Files.createDirectories(outputDir);
			} catch (Exception e) {
				System.err.println("Error creating output directory [%s]: %s".formatted(outputDir, e.getMessage()));
				return 1;
			}
		} else if (!Files.isDirectory(outputDir)) {
			System.err.println("[%s] is not a directory.".formatted(outputDir));
			return 1;
		}

		final RestClient restClient = restClient();
		final List<Path> outputFiles = new ArrayList<>(nodeIds.length);
		final Map<Long, String> errors = new TreeMap<>();

		if (nodeIds.length > 1) {
			try (ProgressBar pb = new ProgressBar("Downloading", nodeIds.length)) {
				for (Long nodeId : nodeIds) {
					try {
						outputFiles.add(downloadNodeCertificate(restClient, nodeId, outputDir));
					} catch (Exception e) {
						errors.put(nodeId, e.getMessage());
					}
					pb.step();
				}
			}
		} else {
			try {
				outputFiles.add(downloadNodeCertificate(restClient, nodeIds[0], outputDir));
			} catch (Exception e) {
				errors.put(nodeIds[0], e.getMessage());
			}
		}

		if (verbosity() > 0) {
			// print out list of saved files
			for (Path file : outputFiles) {
				System.out.println(file);
			}
		}

		if (!errors.isEmpty()) {
			for (Entry<Long, String> e : errors.entrySet()) {
				System.err.println("Error downloading node %d certificate: %s".formatted(e.getKey(), e.getValue()));
			}
			return 2;
		}

		return 1;
	}

	/**
	 * Download a node certificate.
	 * 
	 * @param restClient the REST client
	 * @param nodeId     the ID of the node to download the certificate for
	 * @return the path to the downloaded certificate file
	 */
	public static Path downloadNodeCertificate(RestClient restClient, Long nodeId, Path directory) {
		assert nodeId != null;
		// @formatter:off
		return restClient.get()
			.uri("/solaruser/api/v1/sec/nodes/cert/" +nodeId)
			.accept(MediaType.APPLICATION_OCTET_STREAM)
			.exchange((req, res) -> {
				if (res.getStatusCode().is2xxSuccessful()) {
					ContentDisposition cd = res.getHeaders().getContentDisposition();
					Path outputFile;
					if (cd != null && cd.isAttachment() && cd.getFilename() != null) {
						outputFile = directory.resolve(cd.getFilename());
					} else {
						outputFile = directory.resolve("solarnode-certficiate-%d.p12".formatted(nodeId));
					}
					FileCopyUtils.copy(res.getBody(), Files.newOutputStream(outputFile));
					return outputFile;
				} else {
					throw HttpClientErrorException.create(res.getStatusCode(), res.getStatusText(), res.getHeaders(), new byte[0], null);
				}
			});	
		// @formatter:on
	}

}
