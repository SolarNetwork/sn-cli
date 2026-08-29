package s10k.tool.locations.domain;

import java.time.Instant;
import java.util.Map;

import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * A location request.
 * 
 * @param id         the request ID
 * @param created    the creation date
 * @param modified   the last modification date
 * @param userId     the SolarNetwork account ID that created the request
 * @param status     the request status
 * @param locationId the assigned location ID (once created)
 * @param message    a message
 * @param data       the request information
 */
@RegisterReflectionForBinding
@JsonPropertyOrder({ "id", "created", "modified", "userId", "status", "locationId", "message", "data" })
public record LocationRequest(
// @formatter:off
		Long id,
		
		Instant created,
		
		Instant modified,
		
		Long userId,
		
		LocationRequestStatus status,
		
		@Nullable Long locationId,
		
		@Nullable String message,
		
		@Nullable Map<String, Object> data
		// @formatter:on
) {

}
