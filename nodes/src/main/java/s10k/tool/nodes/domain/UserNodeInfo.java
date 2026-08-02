package s10k.tool.nodes.domain;

import java.time.Instant;
import java.time.ZoneId;

import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * A basic user and node information record.
 * 
 * @author matt
 * @version 1.0
 */
@RegisterReflectionForBinding
@JsonPropertyOrder({ "userId", "nodeId", "name", "description", "locationId", "country", "timeZone",
		"requiresAuthorization", "created" })
public record UserNodeInfo(
// @formatter:off
		  Long nodeId
		, Long userId
		, @Nullable String name
		, @Nullable String description
		, Instant created
		, boolean requiresAuthorization
		, Long locationId
		, String country
		, ZoneId timeZone
		// @formatter:on
) {

	/**
	 * Get the name and description combined with a delimiter.
	 * 
	 * @param delimiter the delimiter to use
	 * @return the name and description combined, or {@code null} if both name and
	 *         description are empty
	 */
	@JsonIgnore
	public @Nullable String nameAndDescription(final String delimiter) {
		final String name = (this.name != null && !this.name.isEmpty() ? this.name : null);
		final String desc = (this.description != null && !this.description.isEmpty() ? this.description : null);
		if (name != null && desc != null) {
			return name + delimiter + desc;
		} else if (name != null) {
			return name;
		}
		return desc;
	}

}
