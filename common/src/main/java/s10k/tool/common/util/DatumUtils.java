package s10k.tool.common.util;

import static net.solarnetwork.util.StringNaturalSortComparator.CASE_INSENSITIVE_NATURAL_SORT;

import java.util.Collection;
import java.util.NavigableMap;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

/**
 * Datum related utilities.
 */
public final class DatumUtils {

	private DatumUtils() {
		super();
		// not available
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
					result.computeIfAbsent(objId, _ -> new TreeSet<>(CASE_INSENSITIVE_NATURAL_SORT)).add(sourceId);
				} catch (NumberFormatException e) {
					// ignore and continue
				}
			}
		}
		return result;
	}

	/**
	 * A case-insensitive wildcard pattern matcher, suitable for source ID patterns.
	 *
	 * <p>
	 * This matcher has caching disabled.
	 * </p>
	 */
	public static final PathMatcher WILDCARD_PATTERN_MATCHER;
	static {
		AntPathMatcher matcher = new AntPathMatcher();
		matcher.setCachePatterns(false);
		matcher.setCaseSensitive(false);
		WILDCARD_PATTERN_MATCHER = matcher;
	}

	private static void filterSources(Set<String> sources, PathMatcher pathMatcher, String pattern,
			SortedSet<String> result) {
		if (sources == null || sources.isEmpty() || pattern == null) {
			return;
		}

		final boolean isPattern = (pathMatcher != null && pathMatcher.isPattern(pattern));
		for (String source : sources) {
			if (isPattern) {
				if (pathMatcher.match(pattern, source)) {
					result.add(source);
				}
			} else if (source.equalsIgnoreCase(pattern)) {
				result.add(source);
			}
		}
	}

	/**
	 * Filter a set of sources using a source ID path pattern.
	 *
	 * <p>
	 * If any arguments are {@code null}, or {@code pathMatcher} is not a path
	 * pattern, then {@code sources} will be returned without filtering.
	 * </p>
	 *
	 * @param sources the sources to filter
	 * @param pattern the pattern to test
	 * @return the filtered sources (always a new instance)
	 */
	public static SortedSet<String> filterSources(Set<String> sources, String pattern) {
		return filterSources(sources, WILDCARD_PATTERN_MATCHER, pattern);
	}

	/**
	 * Filter a set of sources using a source ID path pattern.
	 *
	 * <p>
	 * If any arguments are {@code null}, or {@code pathMatcher} is not a path
	 * pattern, then {@code sources} will be returned without filtering.
	 * </p>
	 *
	 * @param sources     the sources to filter
	 * @param pathMatcher the path matcher to use, or {@code null} for non-pattern
	 *                    matching only
	 * @param pattern     the pattern to test
	 * @return the filtered sources (always a new instance)
	 */
	public static SortedSet<String> filterSources(Set<String> sources, PathMatcher pathMatcher, String pattern) {
		final SortedSet<String> result = new TreeSet<>(CASE_INSENSITIVE_NATURAL_SORT);
		filterSources(sources, pathMatcher, pattern, result);
		return result;
	}

	/**
	 * Filter a set of sources using source ID path patterns.
	 *
	 * <p>
	 * If any arguments are {@code null}, or {@code pathMatcher} is not a path
	 * pattern, then {@code sources} will be returned without filtering.
	 * </p>
	 *
	 * @param sources  the sources to filter
	 * @param patterns the patterns to test (using a logical OR)
	 * @return the filtered sources (always a new instance)
	 */
	public static SortedSet<String> filterSources(Set<String> sources, String[] patterns) {
		return filterSources(sources, WILDCARD_PATTERN_MATCHER, patterns);
	}

	/**
	 * Filter a set of sources using source ID path patterns.
	 *
	 * <p>
	 * If any arguments are {@code null}, or {@code pathMatcher} is not a path
	 * pattern, then {@code sources} will be returned without filtering.
	 * </p>
	 *
	 * @param sources     the sources to filter
	 * @param pathMatcher the path matcher to use, or {@code null} for non-pattern
	 *                    matching only
	 * @param patterns    the patterns to test (using a logical OR)
	 * @return the filtered sources (always a new instance)
	 */
	public static SortedSet<String> filterSources(Set<String> sources, PathMatcher pathMatcher, String[] patterns) {
		final SortedSet<String> result = new TreeSet<>(CASE_INSENSITIVE_NATURAL_SORT);
		if (patterns == null || patterns.length < 1) {
			return result;
		}
		for (String pattern : patterns) {
			filterSources(sources, pathMatcher, pattern, result);
		}
		return result;
	}

}
