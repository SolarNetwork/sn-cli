package s10k.tool.common.domain;

import java.time.ZoneId;
import java.util.Map;

import org.jspecify.annotations.Nullable;

/**
 * API for something that provides a profile.
 */
public interface ProfileProvider {

	/**
	 * Get the profile.
	 * 
	 * @return the profile
	 */
	@Nullable
	ProfileInfo profile();

	/**
	 * Get the profile and verify it has credentials.
	 * 
	 * @return the profile, never {@code null}
	 * @throws IllegalStateException if the profile is not available or does not
	 *                               have credentials
	 */
	default ProfileInfo profileWithCredentials() {
		final ProfileInfo profile = profile();
		if (profile == null || !profile.hasCredentials()) {
			throw new IllegalStateException("No credentials available.");
		}
		return profile;
	}

	/**
	 * Get the profile service URLs.
	 * 
	 * @return the URLs
	 */
	default Map<String, ?> serviceUrls() {
		final var profile = profile();
		return (profile != null ? profile.serviceUrls() : Map.of());
	}

	/**
	 * Get the profile display mode.
	 * 
	 * @return the display mode, or {@code PRETTY}
	 */
	default ResultDisplayMode displayMode() {
		final var profile = profile();
		return (profile != null ? profile.displayMode() : ResultDisplayMode.PRETTY);
	}

	/**
	 * Get the profile pretty style.
	 * 
	 * @return the pretty style, or {@code PrettyStyle.DEFAULT_PRETTY_STYLE}
	 */
	default PrettyStyle prettyStyle() {
		final var profile = profile();
		return (profile != null ? profile.prettyStyle() : PrettyStyle.DEFAULT_PRETTY_STYLE);
	}

	/**
	 * Get the profile time zone.
	 * 
	 * @return the time zone
	 */
	default ZoneId zone() {
		final var profile = profile();
		return (profile != null ? profile.zone() : ZoneId.systemDefault());
	}

	/**
	 * Get the default "include stream aliases" setting.
	 * 
	 * @return {@code true} if stream aliases should be included when querying for
	 *         datum; defaults to {@code true} if not explicitly configured
	 */
	default boolean includeStreamAliases() {
		final var profile = profile();
		return (profile != null ? profile.includeStreamAliases() : true);
	}

}
