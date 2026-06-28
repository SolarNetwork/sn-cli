package s10k.tool.c2c.domain;

import java.time.Instant;
import java.util.Map;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

/**
 * Cloud Datum Stream Rake Task configuration.
 */
@RegisterReflectionForBinding
public record CloudDatumStreamRakeTaskConfiguration(Long configId, Long datumStreamId, String state, Instant executeAt,
		String offset, String message, Map<String, Object> serviceProperties) {

	/**
	 * Get the error count service property.
	 * 
	 * @return the error count
	 */
	public int errorCount() {
		if (serviceProperties != null && serviceProperties.get("errorCount") instanceof Number n) {
			return n.intValue();
		}
		return 0;
	}

}
