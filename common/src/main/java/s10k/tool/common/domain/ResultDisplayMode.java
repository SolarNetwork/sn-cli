package s10k.tool.common.domain;

import org.jspecify.annotations.Nullable;

/**
 * Enumeration of display modes for general result data.
 */
public enum ResultDisplayMode {

	/** A pretty-printed display for humans. */
	PRETTY,

	/** CSV tabular data format for spreadsheets. */
	CSV,

	/** JSON array of arrays format for computers. */
	JSON,

	;

	/**
	 * Parse a boolean/number/string value into an enum value.
	 * 
	 * @param value the value to parse; if {@code null} then {@code PRETTY} will be
	 *              assumed
	 * @return the enum, never {@code null}
	 * @throws IllegalArgumentException if {@code value} is not a valid value
	 */
	public static ResultDisplayMode valueFor(@Nullable Object value) {
		if (value == null) {
			return PRETTY;
		}
		final String s = value.toString();
		for (ResultDisplayMode m : ResultDisplayMode.values()) {
			if (s.equalsIgnoreCase(m.name())) {
				return m;
			}
		}
		throw new IllegalArgumentException("Display mode [%s] is not valid.".formatted(value));
	}

}
