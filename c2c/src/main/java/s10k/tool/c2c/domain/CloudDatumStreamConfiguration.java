package s10k.tool.c2c.domain;

import static java.util.stream.Collectors.joining;
import static net.solarnetwork.util.StringNaturalSortComparator.CASE_INSENSITIVE_NATURAL_SORT;
import static net.solarnetwork.util.StringUtils.commaDelimitedStringToList;
import static net.solarnetwork.util.StringUtils.commaDelimitedStringToMap;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

/**
 * Cloud Datum Stream configuration.
 */
@RegisterReflectionForBinding
public record CloudDatumStreamConfiguration(Long configId, String name, String serviceIdentifier, Instant created,
		Instant modified, boolean enabled, Long datumStreamMappingId, String schedule, String kind, Long objectId,
		String sourceId, Map<String, Object> serviceProperties) {

	/**
	 * Get all source IDs configured on the datum stream.
	 * 
	 * @return the source IDs
	 */
	public SortedSet<String> sourceIds() {
		SortedSet<String> sourceIds = new TreeSet<>(CASE_INSENSITIVE_NATURAL_SORT);
		if (serviceProperties != null) {
			Collection<String> sources = switch (serviceProperties.get("sourceIdMap")) {
			case Map<?, ?> m -> m.values().stream().map(Object::toString).toList();
			case String s -> {
				Map<String, String> map = commaDelimitedStringToMap(s);
				yield (map != null ? map.values() : null);
			}
			case null, default -> null;
			};
			if (sources != null) {
				sourceIds.addAll(sources);
			}
			sources = switch (serviceProperties.get("virtualSourceIds")) {
			case List<?> l -> l.stream().map(Object::toString).toList();
			case String s -> commaDelimitedStringToList(s);
			case null, default -> null;
			};
			if (sources != null) {
				sourceIds.addAll(sources);
			}
		}
		if (serviceProperties == null || !serviceProperties.containsKey("sourceIdMap")) {
			sourceIds.add(sourceId);
		}
		return sourceIds;
	}

	/**
	 * Get a new-line terminated list of source IDs included in this configuration.
	 * 
	 * <p>
	 * This will extract the source IDs referenced in the {@code sourceIdMap}
	 * service property, if available.
	 * </p>
	 * 
	 * @return the source ID list
	 */
	public String sourceIdsValue() {
		SortedSet<String> sourceIds = sourceIds();
		return sourceIds.stream().collect(joining(System.lineSeparator()));
	}

}
