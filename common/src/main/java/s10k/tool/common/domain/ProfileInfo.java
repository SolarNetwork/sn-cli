package s10k.tool.common.domain;

import java.time.ZoneId;
import java.util.Map;

import org.jspecify.annotations.Nullable;

import net.solarnetwork.util.StringUtils;

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
	public PrettyStyle prettyStyle() {
		if (config != null && config.get("pretty-style") instanceof String mode) {
			try {
				return PrettyStyle.fromValue(mode);
			} catch (Exception e) {
				// ignore and fall back to UTC
			}
		}
		return PrettyStyle.DEFAULT_PRETTY_STYLE;
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

	@Override
	public boolean includeStreamAliases() {
		final Object val = (config != null ? config.get("datum-stream-aliases") : null);
		if (val != null) {
			if (val instanceof Boolean b) {
				return b;
			} else if (val instanceof Number n) {
				return n.intValue() != 0;
			}
			return StringUtils.parseBoolean(val.toString());
		}
		return true;
	}

}
