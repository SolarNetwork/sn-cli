/**
 * 
 */
package s10k.tool.nodes.codec;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import net.solarnetwork.codec.JsonDateUtils;
import net.solarnetwork.codec.JsonUtils;
import net.solarnetwork.domain.datum.GeneralDatumMetadata;
import s10k.tool.common.codec.CommonJsonUtils;
import s10k.tool.nodes.domain.NodeMetadata;

/**
 * Deserialize {@link NodeMetadata}.
 */
public class NodeMetadataDeserializer extends StdDeserializer<NodeMetadata> {

	private static final long serialVersionUID = 2287288509607507040L;

	/** A default instance . */
	public static final JsonDeserializer<NodeMetadata> INSTANCE = new NodeMetadataDeserializer();

	/**
	 * Constructor.
	 */
	public NodeMetadataDeserializer() {
		super(NodeMetadata.class);
	}

	@SuppressWarnings("StatementSwitchToExpressionSwitch")
	@Override
	public NodeMetadata deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
		JsonToken t = p.currentToken();
		if (t == JsonToken.VALUE_NULL) {
			return null;
		} else if (p.isExpectedStartObjectToken()) {
			Long nodeId = null;
			Instant created = null;
			Instant updated = null;
			Map<String, Object> m = null;
			Map<String, Map<String, Object>> pm = null;
			Set<String> tags = null;

			String f;
			while ((f = p.nextFieldName()) != null) {
				switch (f) {
				case "nodeId":
					p.nextToken();
					nodeId = p.getLongValue();
					break;

				case "created":
					p.nextToken();
					created = JsonDateUtils.InstantDeserializer.INSTANCE.deserialize(p, ctxt);
					break;

				case "updated":
					p.nextToken();
					updated = JsonDateUtils.InstantDeserializer.INSTANCE.deserialize(p, ctxt);
					break;

				case "m":
					p.nextToken();
					m = p.getCodec().readValue(p, JsonUtils.STRING_MAP_TYPE);
					break;

				case "pm":
					p.nextToken();
					pm = p.getCodec().readValue(p, CommonJsonUtils.STRING_MAP_MAP_TYPE);
					break;

				case "tags":
					p.nextToken();
					break;
				}
			}

			// jump to end object
			while ((t = p.currentToken()) != JsonToken.END_OBJECT) {
				t = p.nextToken();
			}

			var gdm = new GeneralDatumMetadata(m, pm);
			gdm.setTags(tags);
			return new NodeMetadata(nodeId, created, updated, gdm);
		}
		throw new JsonParseException(p, "Unable to parse NodeMetadats (not an object)");
	}

}
