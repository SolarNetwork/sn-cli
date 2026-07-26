package s10k.tool.common.domain;

import java.time.ZoneId;
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
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String, ?> serviceUrls() {
		if (config != null && config.get("service-urls") instanceof Map<?, ?> m) {
			return (Map) m;
		}
		return Map.of();
	}

	@Override
	public ResultDisplayMode displayMode() {
		if (config != null && config.get("display-mode") instanceof String mode) {
			try {
				return ResultDisplayMode.valueFor(mode);
			} catch (Exception e) {
				// ignore and fall back to UTC
			}
		}
		return ResultDisplayMode.PRETTY;
	}

	@Override
	public ZoneId zone() {
		if (config != null && config.get("zone") instanceof String tz) {
			try {
				return ZoneId.of(tz);
			} catch (Exception e) {
				// ignore and fall back to UTC
			}
		}
		return ZoneId.systemDefault();
	}

}
