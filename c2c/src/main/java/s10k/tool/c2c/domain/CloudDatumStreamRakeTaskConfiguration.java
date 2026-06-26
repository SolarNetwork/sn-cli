package s10k.tool.c2c.domain;

import java.time.Instant;
import java.util.Map;

/**
 * Cloud Datum Stream Rake Task configuration.
 */
public record CloudDatumStreamRakeTaskConfiguration(Long configId, Long datumStreamId, String state, Instant executeAt,
		String offset, String message, Map<String, Object> serviceProperties) {

}
