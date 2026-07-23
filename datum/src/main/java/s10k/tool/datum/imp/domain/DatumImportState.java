package s10k.tool.datum.imp.domain;

import org.jspecify.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonCreator;

import s10k.tool.common.domain.ClaimableJobState;

/**
 * The datum import state.
 */
public enum DatumImportState {

	/**
	 * The state is not known.
	 */
	Unknown(ClaimableJobState.Unknown.getKey()),

	/**
	 * The import task has been staged (for testing/previewing), but is not yet
	 * queued.
	 */
	Staged('s'),

	/**
	 * The import task has been cancelled.
	 */
	Retracted('r'),

	/**
	 * The import task has been queued, but not started yet.
	 */
	Queued(ClaimableJobState.Queued.getKey()),

	/**
	 * The import task as been "claimed" for execution but has not started execution
	 * yet.
	 */
	Claimed(ClaimableJobState.Claimed.getKey()),

	/**
	 * The import task is being executed currently.
	 */
	Executing(ClaimableJobState.Executing.getKey()),

	/**
	 * The import task has completed.
	 */
	Completed(ClaimableJobState.Completed.getKey());

	private final char key;

	DatumImportState(char key) {
		this.key = key;
	}

	/**
	 * Get the key value.
	 *
	 * @return the key value
	 */
	public char getKey() {
		return key;
	}

	/**
	 * Get an enum for a key value.
	 *
	 * @param key the key of the enum to get
	 * @return the enum with the given key, or {@link DatumImportState#Unknown} if
	 *         not recognized
	 */
	public static DatumImportState forKey(char key) {
		for (DatumImportState type : DatumImportState.values()) {
			if (type.key == key) {
				return type;
			}
		}
		return DatumImportState.Unknown;
	}

	/**
	 * Get an enum instance for a name or key value.
	 *
	 * @param value the enumeration name or key value, case-insensitive
	 * @return the enum, or {@code null} if value is {@code null} or empty
	 * @throws IllegalArgumentException if {@code value} is not a valid value
	 */
	@JsonCreator
	public static @Nullable DatumImportState fromValue(@Nullable String value) {
		if (value == null || value.isEmpty()) {
			return null;
		}
		final char key = value.length() == 1 ? Character.toLowerCase(value.charAt(0)) : 0;
		for (DatumImportState e : DatumImportState.values()) {
			if (key == e.getKey() || value.equalsIgnoreCase(e.name())) {
				return e;
			}
		}
		throw new IllegalArgumentException("Unknown DatumImportState value [" + value + "]");
	}

}
