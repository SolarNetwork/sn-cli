package s10k.tool.c2c.domain;

import java.util.ArrayList;
import java.util.List;

import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

/**
 * A datum stream mapping detail record.
 */
@RegisterReflectionForBinding
public record CloudDatumStreamMappingDetail(CloudDatumStreamConfiguration datumStream,
		CloudDatumStreamMappingConfiguration mapping, CloudIntegrationConfiguration integration,
		List<CloudDatumStreamMappingPropertyConfiguration> properties) {

	/**
	 * Create a mapping detail record.
	 * 
	 * @param datumStream the datum stream
	 * @param mapping     the mapping
	 * @param integration the integration
	 * @param properties  the optional properties
	 * @return the mapping detail
	 */
	public static CloudDatumStreamMappingDetail mappingDetails(CloudDatumStreamConfiguration datumStream,
			CloudDatumStreamMappingConfiguration mapping, CloudIntegrationConfiguration integration,
			@Nullable List<CloudDatumStreamMappingPropertyConfiguration> properties) {
		var props = new ArrayList<>(properties != null ? properties : List.of());
		return new CloudDatumStreamMappingDetail(datumStream, mapping, integration, props);
	}

}