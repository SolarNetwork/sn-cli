package s10k.tool.c2c.domain;

import java.time.Instant;

/**
 * Cloud Datum Stream Mapping configuration.
 */
public record CloudDatumStreamMappingConfiguration(Long configId, String name, Instant created, Instant modified,
		Long integrationId) {

}
