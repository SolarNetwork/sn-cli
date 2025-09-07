package s10k.tool.instructions.domain;

import java.time.ZonedDateTime;

import net.solarnetwork.domain.Instruction;
import s10k.tool.instructions.util.InstructionsUtils;

/**
 * An instruction request.
 */
public record InstructionRequest(Long nodeId, Instruction instruction, ZonedDateTime expirationDate) {

	/**
	 * Get the instruction topic.
	 * 
	 * @return the topic
	 */
	public String topic() {
		return (instruction != null ? instruction.getTopic() : null);
	}

	/**
	 * Test if the instruction parameters have an {@code executionDate} value.
	 * 
	 * @return {@code true} if the instruction has an {@code executionDate}
	 *         parameter
	 */
	public boolean hasExecutionDate() {
		return (instruction != null && instruction.getParameterValue(InstructionsUtils.EXECUTION_DATE_PARAM) != null);
	}

}
