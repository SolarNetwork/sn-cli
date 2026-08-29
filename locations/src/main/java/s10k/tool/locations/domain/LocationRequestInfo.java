package s10k.tool.locations.domain;

import java.util.Set;

import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import net.solarnetwork.domain.SimpleLocation;

/**
 * A location request information record.
 */
@RegisterReflectionForBinding
@JsonPropertyOrder({ "locationId", "sourceId", "features", "location" })
public record LocationRequestInfo(@Nullable Long locationId, String sourceId, SimpleLocation location,
		Set<String> features) {

}
