package s10k.tool.instructions.cmd;

import java.time.LocalDateTime;
import java.util.Collection;

import net.solarnetwork.domain.InstructionStatus.InstructionState;

/**
 * An instruction filter.
 */
public record InstructionsFilter(Collection<Long> instructionIds, Collection<Long> nodeIds,
		Collection<InstructionState> states, LocalDateTime startDate, LocalDateTime endDate) {

}
