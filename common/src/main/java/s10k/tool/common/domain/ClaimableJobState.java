package s10k.tool.common.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * A "claimable" job state enumeration.
 */
public enum ClaimableJobState {

	/**
	 * The state is not known.
	 */
	Unknown('u'),

	/**
	 * The task has been queued, but not started yet.
	 */
	Queued('q'),

	/**
	 * The task as been "claimed" for execution but has not started execution yet.
	 */
	Claimed('p'),

	/**
	 * The task is being executed currently.
	 */
	Executing('e'),

	/**
	 * The task has completed.
	 */
	Completed('c')

	;

	private final String key;

	ClaimableJobState(char key) {
		this.key = String.valueOf(key);
	}

	/**
	 * Get a unique key for this state.
	 *
	 * @return the state key
	 */
	public char getKey() {
		return key.charAt(0);
	}

	/**
	 * Get a key value for this enum.
	 *
	 * @return the key as a string
	 */
	@JsonValue
	public String keyValue() {
		return key;
	}

	/**
	 * Get an enum for a key value.
	 *
	 * @param key the key of the enum to get
	 * @return the enum with the given key, or {@code Unknown} if not recognized
	 */
	public static ClaimableJobState forKey(char key) {
		for (ClaimableJobState type : ClaimableJobState.values()) {
			if (type.getKey() == key) {
				return type;
			}
		}
		return ClaimableJobState.Unknown;
	}

	/**
	 * Get an enum instance for a name or key value.
	 *
	 * @param value the enumeration name or key value, case-insensitive
	 * @return the enum, or {@code null} if value is {@code null} or empty
	 * @throws IllegalArgumentException if {@code value} is not a valid value
	 */
	@JsonCreator
	public static ClaimableJobState fromValue(String value) {
		if (value == null || value.isEmpty()) {
			return null;
		}
		final char key = value.length() == 1 ? Character.toLowerCase(value.charAt(0)) : 0;
		for (ClaimableJobState e : ClaimableJobState.values()) {
			if (key == e.getKey() || value.equalsIgnoreCase(e.name())) {
				return e;
			}
		}
		throw new IllegalArgumentException("Unknown ClaimableJobState value [" + value + "]");
	}

}
