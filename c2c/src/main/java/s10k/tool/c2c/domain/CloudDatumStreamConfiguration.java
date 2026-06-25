package s10k.tool.c2c.domain;

import java.time.Instant;
import java.util.Map;

/**
 * Cloud Datum Stream configuration.
 */
public record CloudDatumStreamConfiguration(Long configId, String name, String serviceIdentifier, Instant created,
		Instant modified, boolean enabled, Long datumStreamMappingId, String schedule, String kind, Long objectId,
		String sourceId, Map<String, Object> serviceProperties) {

}
