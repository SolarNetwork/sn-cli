package s10k.tool.common.util;

import static java.nio.charset.StandardCharsets.UTF_8;
import static net.solarnetwork.util.StringNaturalSortComparator.CASE_INSENSITIVE_NATURAL_SORT;
import static org.springframework.util.FileCopyUtils.copyToString;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;

import org.jspecify.annotations.Nullable;

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
	public static @Nullable String toStringOrNull(@Nullable Object o) {
		return (o != null ? o.toString() : null);
	}

	/**
	 * Get a string, possibly loaded from a file.
	 * 
	 * <p>
	 * If {@code value} starts with an {@code @} character, that will be stripped
	 * from the string and the remainder treated as a file path, whose contents will
	 * be loaded as a {@code UTF-8} string and returned. Otherwise {@code value}
	 * will be returned as-is.
	 * </p>
	 * 
	 * @param value the value or {@code @}-prefixed file path
	 * @return the resulting string
	 * @throws IllegalStateException if any error occurs reading the file contents
	 */
	public static @Nullable String stringOrFileContents(@Nullable String value) {
		if (value == null || !value.startsWith("@")) {
			return value;
		}
		Path path = Paths.get(value.substring(1));
		try {
			if (!Files.isReadable(path)) {
				throw new IllegalStateException("File [%s] is not available.".formatted(path));
			}
			return copyToString(Files.newBufferedReader(path, UTF_8));
		} catch (IOException e) {
			throw new IllegalStateException("Error reading file [%s]: %s".formatted(path, e.getMessage()));
		}
	}

}
