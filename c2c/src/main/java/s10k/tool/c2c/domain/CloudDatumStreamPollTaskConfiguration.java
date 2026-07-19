package s10k.tool.c2c.domain;

import java.time.Instant;
import java.util.Map;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

import s10k.tool.common.domain.ClaimableJobState;

/**
 * Cloud Datum Stream Poll Task configuration.
 */
@RegisterReflectionForBinding
public record CloudDatumStreamPollTaskConfiguration(Long datumStreamId, String state, Instant executeAt,
		Instant startAt, String message, Map<String, Object> serviceProperties) {

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
	public CloudDatumStreamPollTaskConfiguration copyWithState(ClaimableJobState newState) {
		return new CloudDatumStreamPollTaskConfiguration(datumStreamId, newState.keyValue(), executeAt, startAt,
				message, serviceProperties);
	}

}
