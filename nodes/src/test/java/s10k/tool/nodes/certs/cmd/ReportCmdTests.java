package s10k.tool.nodes.certs.cmd;

import static java.nio.charset.StandardCharsets.UTF_8;
import static net.solarnetwork.util.DateUtils.ISO_DATE_TIME_ALT_UTC;
import static org.assertj.core.api.BDDAssertions.and;
import static org.mockito.BDDMockito.given;

import java.net.URI;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import net.solarnetwork.codec.JsonUtils;
import net.solarnetwork.domain.datum.GeneralDatumMetadata;
import s10k.tool.common.cmd.ToolCmd;
import s10k.tool.common.codec.GeneralDatumMetadataDeserializer;
import s10k.tool.common.codec.GeneralDatumMetadataSerializer;
import s10k.tool.common.util.RestUtils;
import s10k.tool.nodes.codec.NodeCertificateInfoDeserializer;
import s10k.tool.nodes.codec.NodeMetadataDeserializer;
import s10k.tool.nodes.domain.NodeCertificateInfo;
import s10k.tool.nodes.domain.NodeMetadata;
import s10k.tool.test.CommonTestUtils;

/**
 * Test cases for the {@link ReportCmd} class.
 */
@SuppressWarnings("static-access")
@ExtendWith(MockitoExtension.class)
public class ReportCmdTests {

	private static final Long[] TEST_NODE_IDS = new Long[] { 66L, 70L, 100L, 101L };

	private static final String TEST_TOKEN_ID = "test";
	private static final String TEST_TOKEN_SEC = "secret";

	@Mock
	ClientHttpRequestFactory reqFactory;

	private ObjectMapper objectMapper;
	private ReportCmd cmd;

	@BeforeEach
	public void setup() {
		objectMapper = objectMapper();

		var tool = new ToolCmd(TEST_TOKEN_ID, TEST_TOKEN_SEC);
		tool.globalInit(null);

		var certCmd = new CertificatesCmd();
		certCmd.setParentCmd(tool);

		cmd = new ReportCmd(reqFactory, objectMapper);
		cmd.setParentCmd(certCmd);
	}

	private static ObjectMapper objectMapper() {
		ObjectMapper mapper = JsonUtils.newDatumObjectMapper();

		SimpleModule toolModule = new SimpleModule("s10k");
		toolModule.addDeserializer(GeneralDatumMetadata.class, GeneralDatumMetadataDeserializer.INSTANCE);
		toolModule.addDeserializer(NodeMetadata.class, NodeMetadataDeserializer.INSTANCE);
		toolModule.addDeserializer(NodeCertificateInfo.class, NodeCertificateInfoDeserializer.INSTANCE);

		toolModule.addSerializer(GeneralDatumMetadata.class, GeneralDatumMetadataSerializer.INSTANCE);

		mapper.registerModule(toolModule);

		return mapper;
	}

	public static URI listNodesUri() {
		return URI.create(RestUtils.DEFAULT_SOLARNETWORK_BASE_URL + "/solarquery/api/v1/sec/nodes");
	}

	private static String listNodesResponse(Long[] nodeIds) {
		StringBuilder buf = new StringBuilder();
		buf.append("""
				{"success":true, "data":[
				""");
		for (int i = 0; i < nodeIds.length; i++) {
			if (i > 0) {
				buf.append(',');
			}
			buf.append(nodeIds[i]);
		}
		buf.append("]}");
		return buf.toString();
	}

	private static URI viewCertUri(Long nodeId) {
		return URI.create(RestUtils.DEFAULT_SOLARNETWORK_BASE_URL + "/solaruser/api/v1/sec/nodes/cert/" + nodeId);
	}

	private static String viewCertResponse(Long nodeId, Long serialNum, Instant from, Instant to, Instant renew) {
		return """
					{
					"userId": 1,
					"nodeId": %d,
					"certificateSerialNumber": "0x%x",
					"certificateIssuerDN": "CN=CA Signing Certificate,O=SolarNetworkDev",
					"certificateSubjectDN": "UID=%1$d,O=SolarNetworkDev",
					"certificateValidFromDate": "%s",
					"certificateValidUntilDate": "%s",
					"certificateRenewAfterDate": "%s"
				}
				""".formatted(nodeId, serialNum, ISO_DATE_TIME_ALT_UTC.format(from), ISO_DATE_TIME_ALT_UTC.format(to),
				ISO_DATE_TIME_ALT_UTC.format(renew));
	}

	@Test
	public void csvByName() throws Exception {
		// GIVEN
		cmd.certPasswordTable = new ClassPathResource("cert-passwords-01.csv", getClass()).getContentAsString(UTF_8);

		// list node IDs
		final URI listNodesUri = listNodesUri();
		MockClientHttpRequest listNodesReq = new MockClientHttpRequest(HttpMethod.GET, listNodesUri);
		MockClientHttpResponse listNodesRes = new MockClientHttpResponse(
				listNodesResponse(TEST_NODE_IDS).getBytes(UTF_8), HttpStatus.OK);
		listNodesRes.getHeaders().setContentType(MediaType.APPLICATION_JSON);
		listNodesReq.setResponse(listNodesRes);
		given(reqFactory.createRequest(listNodesUri, HttpMethod.GET)).willReturn(listNodesReq);

		long serialNum = CommonTestUtils.randomLong();
		ZonedDateTime from = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS);
		ZonedDateTime to = from.plusDays(7);
		ZonedDateTime renew = to.minusDays(1);

		for (Long nodeId : TEST_NODE_IDS) {
			URI uri = viewCertUri(nodeId);
			MockClientHttpRequest req = new MockClientHttpRequest(HttpMethod.POST, uri);
			MockClientHttpResponse res = new MockClientHttpResponse(
					viewCertResponse(nodeId, serialNum++, from.toInstant(), to.toInstant(), renew.toInstant())
							.getBytes(UTF_8),
					HttpStatus.OK);
			res.getHeaders().setContentType(MediaType.APPLICATION_JSON);
			req.setResponse(res);
			given(reqFactory.createRequest(uri, HttpMethod.POST)).willReturn(req);
		}

		// WHEN
		Integer res = cmd.call();

		// @formatter:off
		and.then(res)
			.as("Result OK")
			.isEqualTo(0)
			;
		// @formatter:on
	}

}
