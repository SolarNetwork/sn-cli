package s10k.tool.common.domain;

import org.jspecify.annotations.Nullable;

import net.solarnetwork.util.StringUtils;

/**
 * An enumeration for "enabled/disabled" state.
 */
public enum EnabledOrDisabled {

	/** An enabled state. */
	Enabled(ClaimableJobState.Queued),

	/** A disabled state. */
	Disabled(ClaimableJobState.Completed),

	;

	private final ClaimableJobState jobState;

	EnabledOrDisabled(ClaimableJobState jobState) {
		this.jobState = jobState;
	}

	/**
	 * Get a job state for this enumeration.
	 * 
	 * @return the job state
	 */
	public ClaimableJobState asJobState() {
		return jobState;
	}

	/**
	 * Parse a
	 * {@code EnabledOrDisabled}/boolean/number/string/{@link ClaimableJobState}
	 * value into an enum value.
	 * 
	 * @param value the value to parse
	 * @return the enum, never {@code null}
	 */
	public static EnabledOrDisabled fromValue(@Nullable Object value) {
		if (value == null) {
			return Disabled;
		} else if (value instanceof Boolean b) {
			return (b ? Enabled : Disabled);
		} else if (value instanceof Number n) {
			return (n.intValue() == 0 ? Disabled : Enabled);
		} else if (value instanceof ClaimableJobState s) {
			return (s == ClaimableJobState.Completed ? Disabled : Enabled);
		}
		final String s = value.toString();
		for (EnabledOrDisabled e : EnabledOrDisabled.values()) {
			if (e.name().equalsIgnoreCase(s)) {
				return e;
			}
		}
		final boolean b = StringUtils.parseBoolean(s);
		return (b ? Enabled : Disabled);
	}

}
