package s10k.tool.instructions.cmd;

import static net.solarnetwork.util.StringUtils.commaDelimitedStringFromCollection;

import java.time.LocalDateTime;
import java.util.Collection;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import net.solarnetwork.domain.InstructionStatus.InstructionState;

/**
 * An instruction filter.
 */
public record InstructionsFilter(Collection<Long> instructionIds, Collection<Long> nodeIds,
		Collection<InstructionState> states, LocalDateTime startDate, LocalDateTime endDate) {

	/**
	 * Get a multi-value map from this filter.
	 * 
	 * @return the multi-value map, suitable for using as request parameters
	 */
	public MultiValueMap<String, Object> toRequestMap() {
		var postBody = new LinkedMultiValueMap<String, Object>(4);
		if (instructionIds != null && !instructionIds.isEmpty()) {
			postBody.set("instructionIds", commaDelimitedStringFromCollection(instructionIds));
		}
		if (nodeIds != null && !nodeIds.isEmpty()) {
			postBody.set("nodeIds", commaDelimitedStringFromCollection(nodeIds));
		}
		if (states != null && !states.isEmpty()) {
			postBody.set("states", commaDelimitedStringFromCollection(states));
		}
		if (startDate != null) {
			postBody.set("startDate", startDate);
		}
		if (endDate != null) {
			postBody.set("endDate", endDate);
		}
		return postBody;
	}

}
