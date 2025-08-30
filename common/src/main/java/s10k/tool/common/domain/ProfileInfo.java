package s10k.tool.common.domain;

/**
 * A profile.
 */
public record ProfileInfo(String name, SnTokenCredentials tokenCredentials) {

	/**
	 * Test if credentials are available.
	 * 
	 * @return {@code true} if credentials are available
	 */
	public boolean hasCredentials() {
		return tokenCredentials != null;
	}

}
