package s10k.tool.common.codec;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import net.solarnetwork.domain.datum.GeneralDatumMetadata;

/**
 * Serializer for {@link GeneralDatumMetadata}.
 */
public class GeneralDatumMetadataSerializer extends StdSerializer<GeneralDatumMetadata> {

	private static final long serialVersionUID = -6209730895522506046L;

	/** A default instance. */
	public static final JsonSerializer<GeneralDatumMetadata> INSTANCE = new GeneralDatumMetadataSerializer();

	/**
	 * Constructor.
	 */
	public GeneralDatumMetadataSerializer() {
		super(GeneralDatumMetadata.class);
	}

	@Override
	public void serialize(GeneralDatumMetadata value, JsonGenerator gen, SerializerProvider provider)
			throws IOException {
		if (value == null) {
			gen.writeNull();
			return;
		}

		final Map<String, Object> m = value.getInfo();
		final Map<String, Map<String, Object>> pm = value.getPropertyInfo();
		final Set<String> tags = value.getTags();

		// @formatter:off
		final int size = (m != null && !m.isEmpty() ? 1 : 0)
				+ (pm != null && !pm.isEmpty()? 1 : 0)
				+ (tags != null && !tags.isEmpty() ? 1 : 0)
				;
		// @formatter:on
		gen.writeStartObject(value, size);

		if (m != null && !m.isEmpty()) {
			gen.writeFieldName("m");
			gen.getCodec().writeValue(gen, m);
		}
		if (pm != null && !pm.isEmpty()) {
			gen.writeFieldName("pm");
			gen.getCodec().writeValue(gen, pm);
		}
		if (tags != null && !tags.isEmpty()) {
			gen.writeFieldName("t");
			gen.getCodec().writeValue(gen, tags);
		}

		gen.writeEndObject();

	}

}
