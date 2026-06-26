package s10k.tool.c2c.domain;

import java.time.Instant;
import java.util.Map;

/**
 * Cloud Datum Stream Poll Task configuration.
 */
public record CloudDatumStreamPollTaskConfiguration(Long datumStreamId, String state, Instant executeAt,
		Instant startAt, String message, Map<String, Object> serviceProperties) {

}
