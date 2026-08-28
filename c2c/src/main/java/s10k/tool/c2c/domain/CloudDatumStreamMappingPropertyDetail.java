package s10k.tool.c2c.domain;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

/**
 * A datum stream property detail record.
 */
@RegisterReflectionForBinding
public record CloudDatumStreamMappingPropertyDetail(CloudDatumStreamConfiguration datumStream,
		CloudDatumStreamMappingConfiguration mapping, CloudIntegrationConfiguration integration,
		CloudDatumStreamMappingPropertyConfiguration property) {

}