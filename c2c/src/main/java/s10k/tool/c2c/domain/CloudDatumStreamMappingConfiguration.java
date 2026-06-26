package s10k.tool.c2c.domain;

import java.time.Instant;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

/**
 * Cloud Datum Stream Mapping configuration.
 */
@RegisterReflectionForBinding
public record CloudDatumStreamMappingConfiguration(Long configId, String name, Instant created, Instant modified,
		Long integrationId) {

}
