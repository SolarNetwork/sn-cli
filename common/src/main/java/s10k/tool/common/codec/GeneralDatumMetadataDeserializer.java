/**
 * 
 */
package s10k.tool.common.codec;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import net.solarnetwork.codec.JsonUtils;
import net.solarnetwork.domain.datum.GeneralDatumMetadata;

/**
 * Deserialize {@link GeneralDatumMetadata}.
 */
public class GeneralDatumMetadataDeserializer extends StdDeserializer<GeneralDatumMetadata> {

	private static final long serialVersionUID = -3685787787120339120L;

	/** A default instance . */
	public static final JsonDeserializer<GeneralDatumMetadata> INSTANCE = new GeneralDatumMetadataDeserializer();

	/**
	 * Constructor.
	 */
	public GeneralDatumMetadataDeserializer() {
		super(GeneralDatumMetadata.class);
	}

	@SuppressWarnings("StatementSwitchToExpressionSwitch")
	@Override
	public GeneralDatumMetadata deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JacksonException {
		JsonToken t = p.currentToken();
		if (t == JsonToken.VALUE_NULL) {
			return null;
		} else if (p.isExpectedStartObjectToken()) {
			Map<String, Object> m = null;
			Map<String, Map<String, Object>> pm = null;
			Set<String> tags = null;

			String f;
			while ((f = p.nextFieldName()) != null) {
				switch (f) {
				case "m":
					p.nextToken();
					m = p.getCodec().readValue(p, JsonUtils.STRING_MAP_TYPE);
					break;

				case "pm":
					p.nextToken();
					pm = p.getCodec().readValue(p, CommonJsonUtils.STRING_MAP_MAP_TYPE);
					break;

				case "t":
					p.nextToken();
					tags = p.getCodec().readValue(p, CommonJsonUtils.STRING_SET_TYPE);
					break;
				}
			}

			// jump to end object
			while ((t = p.currentToken()) != JsonToken.END_OBJECT) {
				t = p.nextToken();
			}

			var gdm = new GeneralDatumMetadata(m, pm);
			gdm.setTags(tags);
			return gdm;
		}
		throw new JsonParseException(p, "Unable to parse GeneralDatumMetadata (not an object)");
	}

}
