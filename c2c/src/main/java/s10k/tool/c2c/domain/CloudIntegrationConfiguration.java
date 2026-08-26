package s10k.tool.c2c.domain;

import static s10k.tool.common.domain.ServiceConfiguration.SERVICE_PROPERTIES_KEY;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

import net.solarnetwork.codec.JsonUtils;

/**
 * Cloud Integration configuration.
 */
@RegisterReflectionForBinding
public record CloudIntegrationConfiguration(Long configId, String name, String serviceIdentifier, Instant created,
		Instant modified, boolean enabled, Map<String, Object> serviceProperties) {

	/**
	 * Get a mapping of this entity's settings.
	 * 
	 * @return the settings
	 */
	public Map<String, Object> toSettings() {
		Map<String, Object> result = new LinkedHashMap<>(9);
		result.put("name", name);
		result.put("serviceIdentifier", serviceIdentifier);
		result.put("enabled", enabled);
		if (serviceProperties != null) {
			// perform a deep copy here
			result.put(SERVICE_PROPERTIES_KEY, JsonUtils.getStringMapFromObject(serviceProperties));
		}
		return result;
	}

}
