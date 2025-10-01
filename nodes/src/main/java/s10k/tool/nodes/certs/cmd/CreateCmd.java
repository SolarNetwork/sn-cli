package s10k.tool.nodes.certs.cmd;

import static s10k.tool.common.util.RestUtils.checkSuccess;

import java.nio.file.Path;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Option;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.util.OutputUtils;
import s10k.tool.common.util.TableUtils;
import s10k.tool.nodes.cmd.ListNodesCmd;
import s10k.tool.nodes.domain.NodeInfo;

/**
 * Create node certificates.
 */
@Component
@Command(name = "create", sortSynopsis = false, header = {
// @formatter:off
		"",
		"""
		Create node certificates.
		
		Use this command to manually create node certificates, without going through the
		normal invitation/association process. This is useful for integrating custom
		data collection applications with SolarNetwork that want to be able to post
		data, and thus require a certificate.
		
		If you provide the --directory option then the newly created certificate will
		be downloaded to the given directory.
		""",
		// @formatter:on
})
public class CreateCmd extends BaseSubCmd<CertificatesCmd> implements Callable<Integer> {

	// @formatter:off
	@Option(names = { "-tz", "--time-zone" },
			description = "a time zone to associate with the node, instead of the local system zone")
	ZoneId zone;

	@Option(names = { "-c", "--country" },
			description = "a 2-character country code",
			required = true)
	String country;

	@Option(names = { "-p", "--password" },
			description = "the certificate keystore password to use",
			required = true,
			interactive = true)
	char[] password;

	@Option(names = { "-d", "--directory" },
			description = "a directory to download the certificate to")
	String outputDirectory;
	
	@Option(names = { "-mode", "--display-mode" },
			description = "how to display the result",
			defaultValue = "PRETTY")
	ResultDisplayMode displayMode = ResultDisplayMode.PRETTY;
	// @formatter:on

	/**
	 * Constructor.
	 * 
	 * @param reqFactory   the HTTP request factory to use
	 * @param objectMapper the mapper to use
	 */
	public CreateCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		final RestClient restClient = restClient();
		try {
			NodeInfo node = createNodeCertificate(restClient, objectMapper, zone, country, password);
			if (node == null) {
				System.err.println("Failed to create node certificate.");
				return 1;
			}

			if (outputDirectory != null) {
				final Path outputDir = OutputUtils.ensureDirectory(outputDirectory);
				if (outputDir != null) {
					Path certPath = DownloadCmd.downloadNodeCertificate(restClient, node.nodeId(), outputDir);
					if (certPath != null && verbosity() > 0) {
						// print out list of saved files
						System.err
								.println(Ansi.AUTO.string("Node certificate saved to @|bold %s|@".formatted(certPath)));
					}
				}
			}

			List<Object[]> tableData = Collections.singletonList(ListNodesCmd.nodeInfoRow(node));
			TableUtils.renderTableData(ListNodesCmd.nodeInfoColumns(), tableData, displayMode, objectMapper,
					TableUtils.TableDataJsonPrettyPrinter.INSTANCE, System.out);
			return 0;
		} catch (Exception e) {
			System.err.println("Error listing node metadata: %s".formatted(e.getMessage()));
		}
		return 1;
	}

	/**
	 * Create a node certificate.
	 * 
	 * @param restClient          the REST client
	 * @param objectMapper        the mapper
	 * @param zone                the desired time zone for the node, or
	 *                            {@code null} to use the system default
	 * @param country             the 2-character country code for the node
	 * @param certificatePassword a password to use for the certificate keystore;
	 *                            will be erased before returning
	 * @return the new node information
	 */
	public static NodeInfo createNodeCertificate(RestClient restClient, ObjectMapper objectMapper, ZoneId zone,
			String country, char[] certificatePassword) {
		assert country != null && certificatePassword != null && certificatePassword.length > 0;

		MultiValueMap<String, Object> body = new LinkedMultiValueMap<>(3);
		body.add("timeZone", (zone != null ? zone : ZoneId.systemDefault()).getId());
		body.add("country", country);
		body.add("keystorePassword", new String(certificatePassword));
		// @formatter:off
		JsonNode response = restClient.post()
			.uri("/solaruser/api/v1/sec/nodes/create-cert")
			.accept(MediaType.APPLICATION_JSON)
			.body(body)
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.retrieve()
			.body(JsonNode.class)
			;	
		// @formatter:on

		Arrays.fill(certificatePassword, '0');

		checkSuccess(response);

		try {
			return objectMapper.treeToValue(response.path("data"), NodeInfo.class);
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("Error parsing NodeInfo from response: %s".formatted(e.getMessage()), e);
		}
	}

}
