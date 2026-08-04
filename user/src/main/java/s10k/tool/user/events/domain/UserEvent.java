package s10k.tool.user.events.domain;

import static net.solarnetwork.util.ObjectUtils.nonnull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;
import java.util.SequencedCollection;
import java.util.UUID;

import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

import com.fasterxml.jackson.annotation.JsonDeserializeAs;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import net.solarnetwork.util.UuidUtils;

/**
 * User event record.
 */
@RegisterReflectionForBinding
@JsonPropertyOrder({ "userId", "eventId", "created", "tags", "message", "data" })
public record UserEvent(// @formatter:off
		  @JsonProperty("userId") Long userId
		, @JsonProperty("eventId") UUID eventId
		, @JsonProperty("tags") @JsonDeserializeAs(value = ArrayList.class) SequencedCollection<String> tags
		, @JsonProperty("message") @Nullable String message
		, @JsonProperty("data") @Nullable Map<String, ?> data
		// @formatter:on
) {

	/**
	 * Get the event creation timestamp.
	 * 
	 * @return the creation timestamp
	 */
	@JsonProperty("created")
	public final Instant eventDate() {
		return nonnull(UuidUtils.extractTimestamp(eventId(), UuidUtils.V7_MICRO_COUNT_PRECISION), "Event ID");
	}

	/**
	 * Test if a specific tag is set.
	 * 
	 * @param tag the tag to look for
	 * @return {@code true} if the {@code tags} array contains {@code tag}
	 */
	@SuppressWarnings("InvalidParam")
	public boolean hasTag(String tag) {
		if (tag == null || tags == null || tags.isEmpty()) {
			return false;
		}
		for (String t : tags) {
			if (t != null && t.equals(tag)) {
				return true;
			}
		}
		return false;
	}

}
