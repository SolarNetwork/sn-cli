package s10k.tool.datum.domain;

import static net.solarnetwork.util.StringUtils.commaDelimitedStringFromCollection;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import net.solarnetwork.domain.datum.ObjectDatumKind;

/**
 * Search filter for datum stream metadata.
 */
public record DatumStreamFilter(Collection<UUID> streamIds, Collection<Long> objectIds, Collection<String> sourceIds,
		Collection<String> propertyNames, Collection<String> instantaneousPropertyNames,
		Collection<String> accumulatingPropertyNames, Collection<String> statusPropertyNames) {

	/**
	 * Create a filter from array parameters.
	 * 
	 * @param streamIds                  the stream IDs, or {@code null}
	 * @param objectIds                  the object (node/location) IDs, or
	 *                                   {@code null}
	 * @param sourceIds                  the source IDs, or {@code null}
	 * @param propertyNames              the property names, or {@code null}
	 * @param instantaneousPropertyNames the instantaneous property names, or
	 *                                   {@code null}
	 * @param accumulatingPropertyNames  the accumulating property names, or
	 *                                   {@code null}
	 * @param statusPropertyNames        the status property names, or {@code null}
	 * @return the new filter instance
	 */
	public static DatumStreamFilter datumStreamFilter(UUID[] streamIds, Long[] objectIds, String[] sourceIds,
			String[] propertyNames, String[] instantaneousPropertyNames, String[] accumulatingPropertyNames,
			String[] statusPropertyNames) {
		// @formatter:off
		return new DatumStreamFilter(
				streamIds != null && streamIds.length > 0 ? Arrays.asList(streamIds) : null,
				objectIds != null && objectIds.length > 0 ? Arrays.asList(objectIds) : null,
				sourceIds != null && sourceIds.length > 0 ? Arrays.asList(sourceIds) : null,
				propertyNames != null && propertyNames.length > 0 ? Arrays.asList(propertyNames) : null,
				instantaneousPropertyNames != null && instantaneousPropertyNames.length > 0 ? Arrays.asList(instantaneousPropertyNames) : null,
				accumulatingPropertyNames != null && accumulatingPropertyNames.length > 0 ? Arrays.asList(accumulatingPropertyNames) : null,
				statusPropertyNames != null && statusPropertyNames.length > 0 ? Arrays.asList(statusPropertyNames) : null
				);
		// @formatter:on
	}

	/**
	 * Test if some property is non-empty.
	 * 
	 * @return {@code true} if at least one property is non-empty
	 */
	public boolean hasAnyCriteria() {
		if (
		// @formatter:off
				   (streamIds == null || streamIds.isEmpty())
				&& (objectIds == null || objectIds.isEmpty())
				&& (sourceIds == null || sourceIds.isEmpty())
				&& (propertyNames == null || propertyNames.isEmpty())
				&& (instantaneousPropertyNames == null || instantaneousPropertyNames.isEmpty())
				&& (accumulatingPropertyNames == null || accumulatingPropertyNames.isEmpty())
				&& (statusPropertyNames == null || statusPropertyNames.isEmpty())
				// @formatter:on
		) {
			return false;
		}
		return true;
	}

	/**
	 * Get a node multi-value map from this filter.
	 * 
	 * @return the multi-value map, suitable for using as request parameters for
	 *         node datum stream metadata
	 */
	public MultiValueMap<String, Object> toNodeRequestMap() {
		return toRequestMap(ObjectDatumKind.Node);
	}

	/**
	 * Get a location multi-value map from this filter.
	 * 
	 * @return the multi-value map, suitable for using as request parameters for
	 *         location datum stream metadata
	 */
	public MultiValueMap<String, Object> toLocationRequestMap() {
		return toRequestMap(ObjectDatumKind.Location);
	}

	/**
	 * Get a multi-value map from this filter.
	 * 
	 * @return the multi-value map, suitable for using as request parameters
	 */
	public MultiValueMap<String, Object> toRequestMap(ObjectDatumKind kind) {
		var postBody = new LinkedMultiValueMap<String, Object>(4);
		if (streamIds != null && !streamIds.isEmpty()) {
			postBody.set("streamIds", commaDelimitedStringFromCollection(streamIds));
		}
		if (objectIds != null && !objectIds.isEmpty()) {
			postBody.set((kind == ObjectDatumKind.Location ? "locationIds" : "nodeIds"),
					commaDelimitedStringFromCollection(objectIds));
		}
		if (sourceIds != null && !sourceIds.isEmpty()) {
			postBody.set("sourceIds", commaDelimitedStringFromCollection(sourceIds));
		}
		if (propertyNames != null && !propertyNames.isEmpty()) {
			postBody.set("propertyNames", commaDelimitedStringFromCollection(propertyNames));
		}
		if (instantaneousPropertyNames != null && !instantaneousPropertyNames.isEmpty()) {
			postBody.set("instantaneousPropertyNames", commaDelimitedStringFromCollection(instantaneousPropertyNames));
		}
		if (accumulatingPropertyNames != null && !accumulatingPropertyNames.isEmpty()) {
			postBody.set("accumulatingPropertyNames", commaDelimitedStringFromCollection(accumulatingPropertyNames));
		}
		if (statusPropertyNames != null && !statusPropertyNames.isEmpty()) {
			postBody.set("statusPropertyNames", commaDelimitedStringFromCollection(statusPropertyNames));
		}
		return postBody;
	}

}
