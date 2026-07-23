package s10k.tool.common.util;

import static com.github.freva.asciitable.HorizontalAlign.LEFT;
import static com.github.freva.asciitable.HorizontalAlign.RIGHT;
import static net.solarnetwork.domain.datum.DatumSamplesType.Accumulating;
import static net.solarnetwork.domain.datum.DatumSamplesType.Instantaneous;
import static net.solarnetwork.domain.datum.DatumSamplesType.Status;
import static net.solarnetwork.util.StringNaturalSortComparator.CASE_INSENSITIVE_NATURAL_SORT;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.SequencedMap;
import java.util.SequencedSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.jspecify.annotations.Nullable;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import com.github.freva.asciitable.Column;

import net.solarnetwork.domain.datum.Datum;
import net.solarnetwork.domain.datum.DatumSamplesOperations;
import net.solarnetwork.domain.datum.DatumSamplesType;

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
	public static NavigableMap<Long, SortedSet<String>> parseStreamIdentifiers(
			@Nullable Collection<String> identifiers) {
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

	private static void filterSources(@Nullable Set<String> sources, @Nullable PathMatcher pathMatcher,
			@Nullable String pattern, SortedSet<String> result) {
		if (sources == null || sources.isEmpty() || pattern == null) {
			return;
		}

		final boolean isPattern = (pathMatcher != null && pathMatcher.isPattern(pattern));
		for (String source : sources) {
			if (isPattern && pathMatcher != null) { // re-check pathMatcher to avoid null warning
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
	public static SortedSet<String> filterSources(@Nullable Set<String> sources, @Nullable String pattern) {
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
	public static SortedSet<String> filterSources(@Nullable Set<String> sources, @Nullable PathMatcher pathMatcher,
			@Nullable String pattern) {
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
	public static SortedSet<String> filterSources(@Nullable Set<String> sources, String @Nullable [] patterns) {
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
	public static SortedSet<String> filterSources(@Nullable Set<String> sources, @Nullable PathMatcher pathMatcher,
			String @Nullable [] patterns) {
		final SortedSet<String> result = new TreeSet<>(CASE_INSENSITIVE_NATURAL_SORT);
		if (patterns == null || patterns.length < 1) {
			return result;
		}
		for (String pattern : patterns) {
			filterSources(sources, pathMatcher, pattern, result);
		}
		return result;
	}

	private static final Set<DatumSamplesType> PROP_TYPES = EnumSet.of(Instantaneous, Accumulating, Status);

	/**
	 * A datum result structure.
	 */
	public record DatumResultStructure(Iterable<Datum> datum,
			SequencedMap<DatumSamplesType, SequencedSet<String>> propertyNames, List<Column> columns) {

		/**
		 * Convert a datum into a tabular structure.
		 * 
		 * @param datum the datum to convert
		 * @return the data
		 */
		public Object[] tableDataRow(Datum datum) {
			List<Object> result = new ArrayList<>(8);
			result.add(datum.getTimestamp() != null ? datum.getTimestamp().toString() : null);
			result.add(datum.getKind() != null ? datum.getKind().getKey() : null);
			result.add(datum.getObjectId());
			result.add(datum.getSourceId());

			final DatumSamplesOperations ops = datum.asSampleOperations();

			for (Entry<DatumSamplesType, SequencedSet<String>> e : propertyNames.entrySet()) {
				DatumSamplesType propType = e.getKey();
				for (String propName : e.getValue()) {
					Object val = ops.getSampleValue(propType, propName);
					result.add(val instanceof BigDecimal num ? num.toPlainString() : val);
				}
			}
			return result.toArray(Object[]::new);
		}

	}

	private static SequencedMap<DatumSamplesType, SequencedSet<String>> resolvePropertyNames(Iterable<Datum> datum) {
		// extract complete set of property names from entire datum set
		SequencedMap<DatumSamplesType, SequencedSet<String>> propNames = new LinkedHashMap<>(3);
		for (Datum d : datum) {
			for (DatumSamplesType propType : PROP_TYPES) {
				Map<String, ?> props = d.asSampleOperations().getSampleData(propType);
				if (props != null) {
					propNames.computeIfAbsent(propType, _ -> new LinkedHashSet<>(4)).addAll(props.keySet());
				}
			}
		}
		return propNames;
	}

	private static List<Column> resolveColumns(Map<DatumSamplesType, SequencedSet<String>> propNames) {
		final List<Column> result = new ArrayList<>(8);
		result.add(new Column().header("Timestamp").dataAlign(LEFT));
		result.add(new Column().header("Kind").dataAlign(RIGHT));
		result.add(new Column().header("Object ID").dataAlign(RIGHT));
		result.add(new Column().header("Source ID").dataAlign(LEFT));

		for (Entry<DatumSamplesType, SequencedSet<String>> e : propNames.entrySet()) {
			DatumSamplesType propType = e.getKey();
			for (String propName : e.getValue()) {
				result.add(new Column().header(propName).dataAlign(propType == Status ? LEFT : RIGHT));
			}
		}
		return result;
	}

	/**
	 * Create a structure for a set of datum, to help with display.
	 * 
	 * @param datum the set of datum
	 * @return the structure
	 */
	public static DatumResultStructure resultStructure(Iterable<Datum> datum) {
		SequencedMap<DatumSamplesType, SequencedSet<String>> propNames = resolvePropertyNames(datum);
		List<Column> columns = resolveColumns(propNames);
		return new DatumResultStructure(datum, propNames, columns);
	}

}
