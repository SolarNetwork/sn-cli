package s10k.tool.nodes.codec;

import java.io.IOException;
import java.io.Serial;
import java.time.Instant;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import net.solarnetwork.codec.JsonDateUtils;
import s10k.tool.nodes.domain.NodeCertificateInfo;

/**
 * Deserializer for {@link NodeCertificateInfo} instances.
 */
public class NodeCertificateInfoDeserializer extends StdDeserializer<NodeCertificateInfo> {

	@Serial
	private static final long serialVersionUID = 1101052116251132348L;

	/** A default instance . */
	public static final JsonDeserializer<NodeCertificateInfo> INSTANCE = new NodeCertificateInfoDeserializer();

	/**
	 * Constructor.
	 */
	public NodeCertificateInfoDeserializer() {
		super(NodeCertificateInfo.class);
	}

	@SuppressWarnings("StatementSwitchToExpressionSwitch")
	@Override
	public NodeCertificateInfo deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JacksonException {
		JsonToken t = p.currentToken();
		if (t == JsonToken.VALUE_NULL) {
			return null;
		} else if (p.isExpectedStartObjectToken()) {
			Long userId = null;
			Long nodeId = null;
			Long serialNum = null;
			String issuerDN = null;
			String subjectDN = null;
			Instant validFrom = null;
			Instant validUntil = null;
			Instant renewAfter = null;

			String f;
			while ((f = p.nextFieldName()) != null) {
				switch (f) {
				case "userId":
					p.nextToken();
					userId = p.getLongValue();
					break;

				case "nodeId":
					p.nextToken();
					nodeId = p.getLongValue();
					break;

				case "certificateSerialNumber": {
					p.nextToken();
					String txt = p.getText();
					if (txt.startsWith("0x")) {
						serialNum = Long.parseLong(txt.substring(2), 16);
					} else {
						serialNum = Long.parseLong(p.getText());
					}
				}
					break;

				case "certificateIssuerDN":
					p.nextToken();
					issuerDN = p.getText();
					break;

				case "certificateSubjectDN":
					p.nextToken();
					subjectDN = p.getText();
					break;

				case "certificateValidFromDate":
					p.nextToken();
					validFrom = JsonDateUtils.InstantDeserializer.INSTANCE.deserialize(p, ctxt);
					break;

				case "certificateValidUntilDate":
					p.nextToken();
					validUntil = JsonDateUtils.InstantDeserializer.INSTANCE.deserialize(p, ctxt);
					break;

				case "certificateRenewAfterDate":
					p.nextToken();
					renewAfter = JsonDateUtils.InstantDeserializer.INSTANCE.deserialize(p, ctxt);
					break;

				default:
					t = p.nextToken();
					break;

				}
			}

			// jump to end object
			while ((t = p.currentToken()) != JsonToken.END_OBJECT) {
				t = p.nextToken();
			}

			final Instant now = Instant.now();
			String status = "OK";
			if (validUntil != null && now.isAfter(validUntil)) {
				status = "Expired";
			} else if (renewAfter != null && now.isAfter(renewAfter)) {
				status = "Renew";
			}

			return new NodeCertificateInfo(userId, nodeId, serialNum, issuerDN, subjectDN, validFrom, validUntil,
					renewAfter, status);
		}
		throw new JsonParseException(p, "Unable to parse NodeCertificateInfo (not an object)");
	}

}
