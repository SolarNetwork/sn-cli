package s10k.tool.c2c.domain;

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

/**
 * A datum stream mapping with its associated properties info record.
 * 
 * @param mapping     the mapping
 * @param integration the integration
 * @param properties  the properties
 */
@RegisterReflectionForBinding
public record CloudDatumStreamMappingInfo(@JsonUnwrapped CloudDatumStreamMappingConfiguration mapping,
		CloudIntegrationConfiguration integration, List<CloudDatumStreamMappingPropertyConfiguration> properties) {

	/**
	 * Create a mapping properties info record.
	 * 
	 * @param mapping     the mapping
	 * @param integration the integration
	 * @param properties  the optional properties
	 * @return the mapping detail
	 */
	public static CloudDatumStreamMappingInfo mappingPropertiesInfo(CloudDatumStreamMappingConfiguration mapping,
			CloudIntegrationConfiguration integration,
			@Nullable List<CloudDatumStreamMappingPropertyConfiguration> properties) {
		var props = new ArrayList<>(properties != null ? properties : List.of());
		return new CloudDatumStreamMappingInfo(mapping, integration, props);
	}

}
