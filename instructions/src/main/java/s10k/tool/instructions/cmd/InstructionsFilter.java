package s10k.tool.instructions.cmd;

import static java.time.ZoneOffset.UTC;
import static net.solarnetwork.util.StringUtils.commaDelimitedStringFromCollection;

import java.time.ZonedDateTime;
import java.util.Collection;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import net.solarnetwork.domain.InstructionStatus.InstructionState;

/**
 * An instruction filter.
 */
public record InstructionsFilter(Collection<Long> instructionIds, Collection<Long> nodeIds,
		Collection<InstructionState> states, ZonedDateTime startDate, ZonedDateTime endDate) {

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
			postBody.set("startDate", startDate.withZoneSameInstant(UTC).toLocalDateTime());
		}
		if (endDate != null) {
			postBody.set("endDate", endDate.withZoneSameInstant(UTC).toLocalDateTime());
		}
		return postBody;
	}

}
