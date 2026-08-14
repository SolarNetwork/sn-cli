package s10k.tool.common.domain;

import org.jspecify.annotations.Nullable;

/**
 * An enumeration of pretty table styles.
 */
public enum PrettyStyle {

	/** A basic style. */
	Basic,

	/** A fancy style. */
	Fancy,

	;

	/** The global default pretty style to use. */
	public static final PrettyStyle DEFAULT_PRETTY_STYLE = PrettyStyle.Basic;

	/**
	 * Parse a {@code PrettyStyle} string value into an enum value.
	 * 
	 * @param value the value to parse
	 * @return the enum, never {@code null}; will default to
	 *         {@code DEFAULT_PRETTY_STYLE} if {@code value} can not be parsed into
	 *         an enum value
	 */
	public static PrettyStyle fromValue(@Nullable Object value) {
		if (value == null) {
			return DEFAULT_PRETTY_STYLE;
		}
		final String s = value.toString();
		for (PrettyStyle e : PrettyStyle.values()) {
			if (e.name().equalsIgnoreCase(s)) {
				return e;
			}
		}
		return DEFAULT_PRETTY_STYLE;
	}

}
