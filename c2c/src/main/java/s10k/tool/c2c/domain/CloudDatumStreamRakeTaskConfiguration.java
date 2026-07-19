package s10k.tool.c2c.domain;

import java.time.Instant;
import java.util.Map;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

import s10k.tool.common.domain.ClaimableJobState;

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

	/**
	 * Create a copy with a specific job state.
	 * 
	 * @param newState the new state
	 * @return the new instance
	 */
	public CloudDatumStreamRakeTaskConfiguration copyWithState(ClaimableJobState newState) {
		return new CloudDatumStreamRakeTaskConfiguration(configId, datumStreamId, newState.keyValue(), executeAt,
				offset, message, serviceProperties);
	}

}
