package s10k.tool.c2c.domain;

import java.time.Instant;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

/**
 * Cloud Datum Stream Mapping configuration.
 */
@RegisterReflectionForBinding
public record CloudDatumStreamMappingPropertyConfiguration(Long datumStreamMappingId, Integer index, Instant created,
		Instant modified, Boolean enabled, String propertyType, String propertyName, String valueType,
		String valueReference) {

}
