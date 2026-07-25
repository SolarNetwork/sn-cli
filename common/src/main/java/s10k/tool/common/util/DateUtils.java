package s10k.tool.common.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import org.jspecify.annotations.Nullable;

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
	public static boolean isMidnight(@Nullable LocalDateTime dateTime) {
		return (dateTime != null && dateTime.truncatedTo(ChronoUnit.DAYS).isEqual(dateTime));
	}

	/**
	 * Get a zoned date time from a local date time and optional time zone.
	 * 
	 * @param date the local date
	 * @param zone the time zone, or {@code null} to use the system default
	 * @return the zoned date time, or {@code null} if {@code date} is {@code null}
	 */
	public static @Nullable ZonedDateTime zonedDate(@Nullable LocalDateTime date, @Nullable ZoneId zone) {
		if (date == null) {
			return null;
		}
		return date.atZone(zone != null ? zone : ZoneId.systemDefault());
	}

	/**
	 * Get an instant, but only if it is not equal to {@link Instant#EPOCH}.
	 * 
	 * @param ts the instant to check
	 * @return the non-epoch instant, or {@code null} if {@code ts} is {@code null}
	 *         or equal to {@link Instant#EPOCH}
	 */
	public static @Nullable Instant nonEpochInstant(@Nullable Instant ts) {
		return (ts == null || ts.compareTo(Instant.EPOCH) == 0 ? null : ts);
	}

	/**
	 * Convert an {@link Instant} into a {@link LocalDateTime} at a specific time
	 * zone.
	 * 
	 * @param timestamp the timestamp to convert
	 * @param zone      the desired zone, or {@code null} to use the system default
	 * @return the local date time, or {@code null} if {@code timestamp} is
	 *         {@code null}
	 */
	public static @Nullable LocalDateTime local(@Nullable Instant timestamp, @Nullable ZoneId zone) {
		if (timestamp == null) {
			return null;
		}
		return timestamp.atZone(zone != null ? zone : ZoneId.systemDefault()).toLocalDateTime();
	}

}
