package s10k.tool.c2c.domain;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

import net.solarnetwork.domain.datum.DatumSamplesType;

/**
 * Cloud Datum Stream Mapping configuration.
 */
@RegisterReflectionForBinding
public record CloudDatumStreamMappingPropertyConfiguration(Long datumStreamMappingId, Integer index, Instant created,
		Instant modified, Boolean enabled, DatumSamplesType propertyType, String propertyName,
		CloudDatumStreamValueType valueType, String valueReference, BigDecimal multiplier, Integer scale) {

}
