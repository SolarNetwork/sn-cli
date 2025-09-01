package s10k.tool.common.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Date helper methods.
 */
public final class DateUtils {

	private DateUtils() {
		// not available
	}

	/**
	 * Test if a local date-time has a time component exactly at midnight.
	 * 
	 * @param dateTime the date-time to test
	 * @return {@code true} if the time component is exactly at midnight
	 */
	public static boolean isMidnight(LocalDateTime dateTime) {
		return (dateTime != null && dateTime.truncatedTo(ChronoUnit.DAYS).isEqual(dateTime));
	}

	/**
	 * Get a zoned date time from a local date time and optional time zone.
	 * 
	 * @param date the local date
	 * @param zone the time zone, or {@code null} to use the system default
	 * @return the zoned date time, or {@code null} if {@code date} is {@code null}
	 */
	public static ZonedDateTime zonedDate(LocalDateTime date, ZoneId zone) {
		if (date == null) {
			return null;
		}
		return date.atZone(zone != null ? zone : ZoneId.systemDefault());
	}

}
