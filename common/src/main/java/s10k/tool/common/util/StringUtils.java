package s10k.tool.common.util;

import static net.solarnetwork.util.StringNaturalSortComparator.CASE_INSENSITIVE_NATURAL_SORT;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Collection;
import java.util.NavigableMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

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

	/**
	 * Naturally sort an array of strings <b>in-place</b>.
	 * 
	 * @param array the array to sort, in place
	 * @return the {@code array} argument, for method chaining
	 */
	public static String[] naturallyCaseInsensitiveSorted(String[] array) {
		if (array == null) {
			return null;
		}
		Arrays.sort(array, CASE_INSENSITIVE_NATURAL_SORT);
		return array;
	}

	/**
	 * Get a string for an optional object.
	 * 
	 * @param o the object to convert to a string, or {@code null}
	 * @return the string or {@code null} if {@code o} is {@code null}
	 */
	public static String toStringOrNull(Object o) {
		return (o != null ? o.toString() : null);
	}

	/**
	 * Parse a set of steam identifiers into a mapping of object ID to associated
	 * source IDs.
	 * 
	 * @param identifiers the stream identifiers to parse, each in the form of
	 *                    {@code OBJECT_ID:SOURCE_ID}
	 * @return the parsed stream ID mappings
	 */
	public static NavigableMap<Long, SortedSet<String>> parseStreamIdentifiers(Collection<String> identifiers) {
		NavigableMap<Long, SortedSet<String>> result = new TreeMap<Long, SortedSet<String>>();
		if (identifiers != null && !identifiers.isEmpty()) {
			for (String identifier : identifiers) {
				if (identifier == null) {
					continue;
				}
				int idx = identifier.indexOf(':');
				if (idx < 1 || idx >= (identifier.length() - 2)) {
					continue;
				}
				String key = identifier.substring(0, idx);
				try {
					Long objId = Long.parseLong(key);
					String sourceId = identifier.substring(idx + 1);
					result.computeIfAbsent(objId, k -> new TreeSet<>(CASE_INSENSITIVE_NATURAL_SORT)).add(sourceId);
				} catch (NumberFormatException e) {
					// ignore and continue
				}
			}
		}
		return result;
	}

}
