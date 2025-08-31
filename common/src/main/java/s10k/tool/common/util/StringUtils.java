package s10k.tool.common.util;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;

import net.solarnetwork.util.DateUtils;

/**
 * Helper methods for strings.
 */
public final class StringUtils {

	private StringUtils() {
		// not available
	}

	/**
	 * Parse a local date time string.
	 * 
	 * <p>
	 * The string must provide a minimum of an ISO 8601 local date, in
	 * {@code YYYY-MM-DD} form. This can be optionally followed by a {@code T} or
	 * space character and then an ISO 8601 time like {@code hh:mm:ss.SSS}.
	 * </p>
	 * 
	 * @param s the date string to parse
	 * @return the parsed date
	 * @throws DateTimeParseException if the date cannot be parsed
	 */
	public static LocalDateTime parseLocalDateTime(String s) {
		if (s == null) {
			return null;
		}
		try {
			return parseLocalDateTime(DateUtils.ISO_DATE_OPT_TIME_LOCAL.parse(s));
		} catch (DateTimeParseException e) {
			return parseLocalDateTime(DateUtils.ISO_DATE_OPT_TIME_ALT_LOCAL.parse(s));
		}
	}

	private static LocalDateTime parseLocalDateTime(TemporalAccessor ta) {
		try {
			return LocalDateTime.from(ta);
		} catch (DateTimeException e) {
			return LocalDate.from(ta).atStartOfDay(ZoneId.from(ta)).toLocalDateTime();
		}
	}

}
