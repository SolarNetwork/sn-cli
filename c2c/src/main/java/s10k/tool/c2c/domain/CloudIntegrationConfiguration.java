package s10k.tool.c2c.domain;

import java.time.Instant;
import java.util.Map;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

/**
 * Cloud Integration configuration.
 */
@RegisterReflectionForBinding
public record CloudIntegrationConfiguration(Long configId, String name, String serviceIdentifier, Instant created,
		Instant modified, boolean enabled, Map<String, Object> serviceProperties) {

}
