package s10k.tool.c2c.domain;

import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

/**
 * A datum stream property info record.
 */
@RegisterReflectionForBinding
public record CloudDatumStreamMappingPropertyInfo(@JsonUnwrapped CloudDatumStreamMappingConfiguration mapping,
		CloudIntegrationConfiguration integration, @Nullable CloudDatumStreamMappingPropertyConfiguration property) {

}