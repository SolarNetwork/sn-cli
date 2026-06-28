package s10k.tool.c2c.domain;

import static java.util.stream.Collectors.joining;
import static net.solarnetwork.util.StringNaturalSortComparator.CASE_INSENSITIVE_NATURAL_SORT;

import java.time.Instant;
import java.util.Map;

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
		if (serviceProperties != null && serviceProperties.get("sourceIdMap") instanceof Map<?, ?> m) {
			return m.values().stream().map(Object::toString).sorted(CASE_INSENSITIVE_NATURAL_SORT)
					.collect(joining(System.lineSeparator()));
		}
		return sourceId;
	}

}
