package s10k.tool.c2c.domain;

import static java.util.stream.Collectors.joining;
import static net.solarnetwork.util.StringNaturalSortComparator.CASE_INSENSITIVE_NATURAL_SORT;
import static net.solarnetwork.util.StringUtils.commaDelimitedStringToList;
import static net.solarnetwork.util.StringUtils.commaDelimitedStringToMap;

import java.time.Instant;
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
		SortedSet<String> sourceIds = new TreeSet<>(CASE_INSENSITIVE_NATURAL_SORT);
		if (serviceProperties != null) {
			switch (serviceProperties.get("sourceIdMap")) {
			case Map<?, ?> m -> sourceIds.addAll(m.values().stream().map(Object::toString).toList());
			case String s -> sourceIds.addAll(commaDelimitedStringToMap(s).values());
			case null, default -> {
			}
			}
			switch (serviceProperties.get("virtualSourceIds")) {
			case List<?> l -> sourceIds.addAll(l.stream().map(Object::toString).toList());
			case String s -> sourceIds.addAll(commaDelimitedStringToList(s));
			case null, default -> {
			}
			}
		}
		if (serviceProperties == null || !serviceProperties.containsKey("sourceIdMap")) {
			sourceIds.add(sourceId);
		}
		return sourceIds.stream().collect(joining(System.lineSeparator()));
	}

}
