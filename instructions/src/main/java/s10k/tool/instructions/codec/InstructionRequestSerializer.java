/**
 * 
 */
package s10k.tool.instructions.codec;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import net.solarnetwork.codec.BasicInstructionField;
import net.solarnetwork.codec.JsonDateUtils.InstantSerializer;
import net.solarnetwork.domain.Instruction;
import s10k.tool.instructions.domain.InstructionRequest;

/**
 * Serializer for {@link InstructionRequest}.
 */
public class InstructionRequestSerializer extends StdSerializer<InstructionRequest> {

	private static final long serialVersionUID = 4405766780201856356L;

	/** A default instance. */
	public static final JsonSerializer<InstructionRequest> INSTANCE = new InstructionRequestSerializer();

	/**
	 * Constructor.
	 */
	public InstructionRequestSerializer() {
		super(InstructionRequest.class);
	}

	@Override
	public void serialize(InstructionRequest value, JsonGenerator gen, SerializerProvider provider) throws IOException {
		if (value == null) {
			gen.writeNull();
			return;
		}
		final Instruction instr = value.instruction();
		final Map<String, List<String>> params = instr.getParameterMultiMap();

		// @formatter:off
		final int size = (value.nodeId() != null ? 1 : 0)
				+ (value.expirationDate() != null ? 1 : 0)
				+ (params != null && !params.isEmpty() ? 1 : 0)
				;
		// @formatter:on
		gen.writeStartObject(instr, size);
		if (value.nodeId() != null) {
			gen.writeNumberField("nodeId", value.nodeId());
		}
		if (value.expirationDate() != null) {
			gen.writeFieldName("expirationDate");
			InstantSerializer.INSTANCE.serialize(value.expirationDate().toInstant(), gen, provider);
		}
		BasicInstructionField.Params.writeValue(gen, provider, params);
		gen.writeEndObject();
	}

}
