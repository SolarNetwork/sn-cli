package s10k.tool.common.domain;

/**
 * API for something that provides a profile.
 */
public interface ProfileProvider {

	/**
	 * Get the profile.
	 * 
	 * @return the profile
	 */
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

}
