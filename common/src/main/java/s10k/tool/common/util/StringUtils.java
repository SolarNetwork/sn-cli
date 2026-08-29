package s10k.tool.common.util;

import static java.nio.charset.StandardCharsets.UTF_8;
import static net.solarnetwork.util.StringNaturalSortComparator.CASE_INSENSITIVE_NATURAL_SORT;
import static org.springframework.util.FileCopyUtils.copyToString;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.DateTimeException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import org.jspecify.annotations.Nullable;

import net.solarnetwork.domain.SortDescriptor;
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
	public static @Nullable LocalDateTime parseLocalDateTime(@Nullable String s) {
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
	 * Parse a local date time string into an instant at a given time zone.
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
	public static @Nullable Instant parseLocalTimestamp(@Nullable String s, ZoneId zone) {
		final LocalDateTime ts = StringUtils.parseLocalDateTime(s);
		return (ts != null ? ts.atZone(zone).toInstant() : null);
	}

	/**
	 * Naturally sort an array of strings <b>in-place</b>.
	 * 
	 * @param array the array to sort, in place
	 * @return the {@code array} argument, for method chaining
	 */
	public static String @Nullable [] naturallyCaseInsensitiveSorted(String @Nullable [] array) {
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

	/**
	 * Get a boolean as a string, but only if it is {@code true}.
	 * 
	 * @param val the boolean to test
	 * @return the string {@code true} if {@code val} is true
	 */
	public static @Nullable String onlyTrueValue(@Nullable Boolean val) {
		return (val != null && val.booleanValue() ? String.valueOf(true) : null);
	}

	/**
	 * Lookup bundle values that case-insenstive match any of a set of substring
	 * queries.
	 * 
	 * @param prefix  a prefix the bundle key must have
	 * @param suffix  a prefix the bundle key must have
	 * @param queries the substrings to look for
	 * @param queries the queries to search for
	 * @return the list of matching bundle values, never {@code null}
	 */
	public static List<String> findBundleKeys(final ResourceBundle bundle, final @Nullable String prefix,
			final @Nullable String suffix, final String @Nullable [] queries) {
		if (queries == null || queries.length < 0) {
			return List.of();
		}
		final List<String> result = new ArrayList<>(queries.length);
		for (String query : queries) {
			final String lcQuery = query.toLowerCase(Locale.ENGLISH);
			for (String key : bundle.keySet()) {
				if (prefix != null && !key.startsWith(prefix)) {
					continue;
				}
				if (suffix != null && !key.endsWith(suffix)) {
					continue;
				}
				final String val = bundle.getString(key);
				if (key.contains(lcQuery) || val.toLowerCase(Locale.getDefault()).contains(lcQuery)) {
					result.add(key.substring((prefix != null ? prefix.length() : 0),
							key.length() - (suffix != null ? suffix.length() : 0)));
				}
			}
		}
		return result;
	}

	/**
	 * Lookup a bundle entry using a case-insensitive substring search.
	 * 
	 * @param query  the substring to look for
	 * @param prefix a prefix the bundle key must have; will be stripped from the
	 *               resulting entry key
	 * @param suffix a prefix the bundle key must have; will be stripped from the
	 *               resulting entry key
	 * @return the first matching entry
	 * @throws IllegalStateException if a matching entry is not found
	 */
	public static Entry<String, String> findBundleEntry(final ResourceBundle bundle, final @Nullable String query,
			final @Nullable String prefix, String suffix) {
		if (query == null || query.isEmpty()) {
			throw new IllegalStateException("Datum stream type not provided.");
		}
		final String lcQuery = query.toLowerCase(Locale.ENGLISH);
		for (String key : bundle.keySet()) {
			if (prefix != null && !key.startsWith(prefix)) {
				continue;
			}
			if (suffix != null && !key.endsWith(suffix)) {
				continue;
			}
			final String val = bundle.getString(key);
			if (key.contains(lcQuery) || val.toLowerCase(Locale.getDefault()).contains(lcQuery)) {
				return Map.entry(key.substring((prefix != null ? prefix.length() : 0),
						key.length() - (suffix != null ? suffix.length() : 0)), val);
			}
		}
		throw new IllegalStateException("Value not found for [" + query + "]");
	}

	/**
	 * Get a list of valid ordering keys.
	 * 
	 * @param <E>   the enum type with the valid order keys
	 * @param keys  the order keys to validate
	 * @param clazz the enum class with the valid order keys
	 * @return the order by list, or {@code null} if no valid keys are provided
	 */
	public static <E extends Enum<E>> @Nullable List<String> orderByList(final String @Nullable [] keys,
			Class<E> clazz) {
		if (keys == null || keys.length < 1) {
			return null;
		}
		final List<String> result = new ArrayList<>(keys.length);
		for (String key : keys) {
			final boolean descending = key.endsWith("~") && key.length() > 1;
			final String k = (descending ? key.substring(0, key.length() - 1) : key);
			for (E enumVal : clazz.getEnumConstants()) {
				if (k.equalsIgnoreCase(enumVal.name())) {
					result.add(enumVal.name().substring(0, 1).toLowerCase(Locale.ROOT) + enumVal.name().substring(1));
				}
			}
		}
		return (!result.isEmpty() ? result : null);
	}

	/**
	 * Convert a collection of {@code SortDescriptor} into a list of {@code orderBy}
	 * values.
	 * 
	 * @param sorts the sort descriptors to convert
	 * @return the order by list
	 */
	public static List<String> orderByList(final Collection<? extends SortDescriptor> sorts) {
		final List<String> result = new ArrayList<>(sorts.size());
		for (SortDescriptor sort : sorts) {
			String key = sort.getSortKey();
			if (key == null || key.isEmpty()) {
				continue;
			}
			if (sort.isDescending()) {
				key += '~';
			}
			result.add(key);

		}
		return result;
	}

}
