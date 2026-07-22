package s10k.tool.common.domain;

import java.util.Map;

import org.jspecify.annotations.Nullable;

/**
 * A profile.
 */
public record ProfileInfo(String name, SnTokenCredentials tokenCredentials, @Nullable Map<String, ?> config)
		implements ProfileProvider {

	/**
	 * Test if credentials are available.
	 * 
	 * @return {@code true} if credentials are available
	 */
	public boolean hasCredentials() {
		return tokenCredentials != null;
	}

	@Override
	public ProfileInfo profile() {
		return this;
	}

	/**
	 * Get a mapping of service URLs.
	 * 
	 * @return the service URL mapping
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String, ?> serviceUrls() {
		if (config != null && config.get("service-urls") instanceof Map<?, ?> m) {
			return (Map) m;
		}
		return Map.of();
	}

}
