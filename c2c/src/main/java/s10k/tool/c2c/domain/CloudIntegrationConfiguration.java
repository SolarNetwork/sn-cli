package s10k.tool.c2c.domain;

import java.time.Instant;
import java.util.Map;

/**
 * Cloud Integration configuration.
 */
public record CloudIntegrationConfiguration(Long configId, String name, String serviceIdentifier, Instant created,
		Instant modified, boolean enabled, Map<String, Object> serviceProperties) {

}
