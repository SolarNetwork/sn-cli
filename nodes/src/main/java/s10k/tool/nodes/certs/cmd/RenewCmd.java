package s10k.tool.nodes.certs.cmd;

import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.util.OutputUtils;
import s10k.tool.common.util.TableUtils;
import s10k.tool.nodes.domain.NodeCertificateInfo;

/**
 * Renew a node certificate.
 */
@Component
@Command(name = "renew", sortSynopsis = false, header = {
// @formatter:off
		"",
		"""
		Renew node certificate.
		""",
		// @formatter:on
})
public class RenewCmd extends BaseSubCmd<CertificatesCmd> implements Callable<Integer> {

	// @formatter:off
	@Option(names = { "-node", "--node-id" },
			description = "a node ID to renew the certificate for",
			required = true)
	Long nodeId;

	@Option(names = { "-p", "--password" },
			description = "the certificate keystore password",
			required = true,
			interactive = true)
	String password;

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
	public RenewCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		final RestClient restClient = restClient();
		final ZoneId zone = ZoneId.systemDefault();
		try {
			NodeCertificateInfo info = renewNodeCertificate(restClient, nodeId, new String(password));

			if (displayMode == ResultDisplayMode.JSON) {
				OutputUtils.writeJsonObject(objectMapper, info);
			} else {
				List<?> tableData = Collections.singletonList(ReportCmd.reportRow(info, zone));
				TableUtils.renderTableData(ReportCmd.reportColumns(), tableData, displayMode, objectMapper,
						TableUtils.TableDataJsonPrettyPrinter.INSTANCE, System.out);
			}
			return 0;
		} catch (Exception e) {
			System.err.println("Error listing node metadata: %s".formatted(e.getMessage()));
		}
		return 1;
	}

	/**
	 * Renew a node certificate.
	 * 
	 * @param restClient the REST client
	 * @param nodeId     the node ID
	 * @param password   the certificate password
	 * @return the certificate info
	 */
	public static NodeCertificateInfo renewNodeCertificate(RestClient restClient, Long nodeId, String password) {
		assert nodeId != null;
		if (password == null || password.isBlank()) {
			return NodeCertificateInfo.missingPassword(nodeId);
		}
		MultiValueMap<String, Object> postBody = new LinkedMultiValueMap<>(1);
		postBody.add("password", password);

		try {
			// @formatter:off
			return restClient.post()
				.uri("/solaruser/api/v1/sec/nodes/cert/renew" +nodeId)
				.body(postBody)
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.body(NodeCertificateInfo.class)
				;		
			// @formatter:on
		} catch (HttpClientErrorException e) {
			if (e.getStatusCode().isSameCodeAs(HttpStatus.FORBIDDEN)) {
				JsonNode response = e.getResponseBodyAs(JsonNode.class);
				String msg = response.path("message").asText();
				if (msg.toLowerCase(Locale.ENGLISH).contains("password")) {
					return NodeCertificateInfo.invalidPassword(nodeId);
				}
				return NodeCertificateInfo.forbidden(nodeId);
			}
			return NodeCertificateInfo.error(nodeId, e.getMessage());
		}
	}

}
