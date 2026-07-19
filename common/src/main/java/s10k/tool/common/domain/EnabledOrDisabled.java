package s10k.tool.common.domain;

import org.jspecify.annotations.Nullable;

import net.solarnetwork.util.StringUtils;

/**
 * An enumeration for "enabled/disabled" state.
 */
public enum EnabledOrDisabled {

	/** An enabled state. */
	Enabled,

	/** A disabled state. */
	Disabled,

	;

	/**
	 * Parse a boolean/number/string value into an enum value.
	 * 
	 * @param value the value to parse
	 * @return the enum, never {@code null}
	 */
	public static EnabledOrDisabled valueFor(@Nullable Object value) {
		if (value == null) {
			return Disabled;
		} else if (value instanceof Boolean b) {
			return (b ? Enabled : Disabled);
		} else if (value instanceof Number n) {
			return (n.intValue() == 0 ? Disabled : Enabled);
		}
		final boolean b = StringUtils.parseBoolean(value.toString());
		return (b ? Enabled : Disabled);
	}

}
