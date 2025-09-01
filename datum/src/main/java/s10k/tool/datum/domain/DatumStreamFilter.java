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
public record DatumStreamFilter(Collection<UUID> streamIds, Collection<Long> objectIds, Collection<String> sourceIds) {

	/**
	 * Create a filter from array parameters.
	 * 
	 * @param streamIds the stream IDs, or {@code null}
	 * @param objectIds the object (node/location) IDs, or {@code null}
	 * @param sourceIds the source IDs, or {@code null}
	 * @return the new filter instance
	 */
	public static DatumStreamFilter datumStreamFilter(UUID[] streamIds, Long[] objectIds, String[] sourceIds) {
		// @formatter:off
		return new DatumStreamFilter(
				streamIds != null && streamIds.length > 0 ? Arrays.asList(streamIds) : null,
				objectIds != null && objectIds.length > 0 ? Arrays.asList(objectIds) : null,
				sourceIds != null && sourceIds.length > 0 ? Arrays.asList(sourceIds) : null
				);
		// @formatter:on
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
		return postBody;
	}

}
