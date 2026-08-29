package s10k.tool.locations.domain;

/**
 * Location request status enumeration.
 */
public enum LocationRequestStatus {

	/** The request has been submitted and received. */
	Submitted('s'),

	/** The request has been rejected. */
	Rejected('r'),

	/** The request was found to be a duplicate for an existing location. */
	Duplicate('d'),

	/** The location has been created. */
	Created('c'),

	;

	private final char code;

	LocationRequestStatus(char code) {
		this.code = code;
	}

	/**
	 * Get the status code.
	 * 
	 * @return the code
	 */
	public char getCode() {
		return code;
	}

}
