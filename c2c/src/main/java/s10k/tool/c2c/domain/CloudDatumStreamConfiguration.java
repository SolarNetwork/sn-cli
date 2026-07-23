package s10k.tool.c2c.domain;

import static java.util.stream.Collectors.joining;
import static net.solarnetwork.util.StringNaturalSortComparator.CASE_INSENSITIVE_NATURAL_SORT;
import static net.solarnetwork.util.StringUtils.commaDelimitedStringToList;
import static net.solarnetwork.util.StringUtils.commaDelimitedStringToMap;
import static s10k.tool.common.domain.ServiceConfiguration.SERVICE_PROPERTIES_KEY;

import java.time.Instant;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

import net.solarnetwork.codec.JsonUtils;
import net.solarnetwork.domain.Differentiable;
import net.solarnetwork.domain.datum.ObjectDatumKind;

/**
 * Cloud Datum Stream configuration.
 */
@RegisterReflectionForBinding
public record CloudDatumStreamConfiguration(Long configId, String name, String serviceIdentifier, Instant created,
		Instant modified, boolean enabled, @Nullable Long datumStreamMappingId, @Nullable String schedule,
		ObjectDatumKind kind, @Nullable Long objectId, @Nullable String sourceId,
		@Nullable Map<String, Object> serviceProperties) implements Differentiable<CloudDatumStreamConfiguration> {

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

	/**
	 * Get a mapping of this entity's settings.
	 * 
	 * @return the settings
	 */
	public Map<String, Object> toSettings() {
		Map<String, Object> result = new LinkedHashMap<>(9);
		result.put("name", name);
		result.put("serviceIdentifier", serviceIdentifier);
		result.put("enabled", enabled);
		result.put("kind", kind.keyValue());
		if (datumStreamMappingId != null) {
			result.put("datumStreamMappingId", datumStreamMappingId);
		}
		if (schedule != null) {
			result.put("schedule", schedule);
		}
		if (objectId != null) {
			result.put("objectId", objectId);
		}
		if (sourceId != null) {
			result.put("sourceId", sourceId);
		}
		if (serviceProperties != null) {
			// perform a deep copy here
			result.put(SERVICE_PROPERTIES_KEY, JsonUtils.getStringMapFromObject(serviceProperties));
		}
		return result;
	}

	/**
	 * Test if another settings map differs from the settings on this instance.
	 * 
	 * @param other the other settings
	 * @return {@code true} if {@code other} is not equal to {@link #toSettings()}
	 */
	public boolean differsFromSettings(@Nullable Map<String, ?> other) {
		return (other == null || !other.equals(toSettings()));
	}

	@Override
	public boolean differsFrom(@Nullable CloudDatumStreamConfiguration other) {
		return differsFromSettings(other != null ? other.toSettings() : null);
	}

}
