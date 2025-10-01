package s10k.tool.nodes.codec;

import static net.solarnetwork.codec.JsonUtils.parseDateAttribute;
import static net.solarnetwork.util.DateUtils.ISO_DATE_TIME_ALT_UTC;

import java.io.IOException;
import java.io.Serial;
import java.time.Instant;

import org.springframework.boot.jackson.JsonObjectDeserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import net.solarnetwork.domain.BasicIdentityLocation;
import net.solarnetwork.domain.Location;
import s10k.tool.nodes.domain.NodeInfo;

/**
 * Deserialize {@link NodeInfo}.
 */
public class NodeInfoDeserializer extends JsonObjectDeserializer<NodeInfo> {

	@Serial
	private static final long serialVersionUID = -6077421032994564650L;

	/** A default instance . */
	public static final JsonDeserializer<NodeInfo> INSTANCE = new NodeInfoDeserializer();

	/**
	 * Constructor.
	 */
	public NodeInfoDeserializer() {
		super();
	}

	@SuppressWarnings("StatementSwitchToExpressionSwitch")
	@Override
	protected NodeInfo deserializeObject(final JsonParser jsonParser, final DeserializationContext context,
			final ObjectCodec codec, final JsonNode tree) throws IOException {
		JsonNode tmp = tree.path("id");
		if (!tmp.isNumber()) {
			tmp = tree.path("node").path("id");
			if (!tmp.isNumber()) {
				return null;
			}
		}

		Long nodeId = tmp.longValue();
		Instant created = parseDateAttribute(tree, "created", ISO_DATE_TIME_ALT_UTC, Instant::from);
		boolean reqAuth = tree.path("requiresAuthorization").asBoolean();

		Long ownerId = null;
		tmp = tree.path("userId");
		if (tmp.isNumber()) {
			ownerId = tmp.longValue();
		}

		String ownerEmail = null;
		tmp = tree.path("user").path("email");
		if (tmp.isTextual()) {
			ownerEmail = tmp.textValue();
		}

		Location loc = null;
		tmp = tree.path("nodeLocation");
		if (tmp.isObject()) {
			try {
				loc = codec.treeToValue(tmp, BasicIdentityLocation.class);
			} catch (JsonProcessingException e) {
				// ignore and continue
			}
		}

		return new NodeInfo(nodeId, created, reqAuth, ownerId, ownerEmail, loc);
	}

}
