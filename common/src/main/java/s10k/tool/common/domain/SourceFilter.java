package s10k.tool.common.domain;

import static java.time.ZoneOffset.UTC;
import static net.solarnetwork.util.StringUtils.commaDelimitedStringFromCollection;
import static s10k.tool.common.util.DateUtils.isMidnight;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import net.solarnetwork.domain.datum.ObjectDatumKind;

/**
 * Search filter for source information.
 */
public record SourceFilter(Collection<Long> objectIds, Collection<String> sourceIds, ZonedDateTime startDate,
		ZonedDateTime endDate, boolean useLocalDates, String metadataFilter, Collection<String> propertyNames,
		Collection<String> instantaneousPropertyNames, Collection<String> accumulatingPropertyNames,
		Collection<String> statusPropertyNames) {

	/**
	 * Create a filter from array parameters.
	 * 
	 * @param objectIds                  the object (node/location) IDs, or
	 *                                   {@code null}
	 * @param sourceIds                  the source IDs, or {@code null}
	 * @param startDate                  the minimum date the node must have data
	 *                                   after
	 * @param endDate                    the maximum date (exclusive) the node must
	 *                                   have data after
	 * @param useLocalDates              {@code true} to treat the min/max dates as
	 *                                   node local, otherwise as UTC
	 * @param metadataFilter             a metadata filter
	 * @param propertyNames              the property names, or {@code null}
	 * @param instantaneousPropertyNames the instantaneous property names, or
	 *                                   {@code null}
	 * @param accumulatingPropertyNames  the accumulating property names, or
	 *                                   {@code null}
	 * @param statusPropertyNames        the status property names, or {@code null}
	 * @return the new filter instance
	 */
	public static SourceFilter sourceFilter(Long[] objectIds, String[] sourceIds, ZonedDateTime startDate,
			ZonedDateTime endDate, boolean useLocalDates, String metadataFilter, String[] propertyNames,
			String[] instantaneousPropertyNames, String[] accumulatingPropertyNames, String[] statusPropertyNames) {
		// @formatter:off
		return new SourceFilter(
				objectIds != null && objectIds.length > 0 ? Arrays.asList(objectIds) : null,
				sourceIds != null && sourceIds.length > 0 ? Arrays.asList(sourceIds) : null,
				startDate,
				endDate,
				useLocalDates,
				metadataFilter,
				propertyNames != null && propertyNames.length > 0 ? Arrays.asList(propertyNames) : null,
				instantaneousPropertyNames != null && instantaneousPropertyNames.length > 0 ? Arrays.asList(instantaneousPropertyNames) : null,
				accumulatingPropertyNames != null && accumulatingPropertyNames.length > 0 ? Arrays.asList(accumulatingPropertyNames) : null,
				statusPropertyNames != null && statusPropertyNames.length > 0 ? Arrays.asList(statusPropertyNames) : null
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
		if (objectIds != null && !objectIds.isEmpty()) {
			postBody.set((kind == ObjectDatumKind.Location ? "locationIds" : "nodeIds"),
					commaDelimitedStringFromCollection(objectIds));
		}
		if (sourceIds != null && !sourceIds.isEmpty()) {
			postBody.set("sourceIds", commaDelimitedStringFromCollection(sourceIds));
		}
		if (startDate != null) {
			LocalDateTime dt;
			if (useLocalDates) {
				dt = startDate.toLocalDateTime();
			} else {
				dt = startDate.withZoneSameInstant(UTC).toLocalDateTime();
			}
			postBody.set(useLocalDates ? "localStartDate" : "startDate", isMidnight(dt) ? dt.toLocalDate() : dt);
		}
		if (endDate != null) {
			LocalDateTime dt;
			if (useLocalDates) {
				dt = endDate.toLocalDateTime();
			} else {
				dt = endDate.withZoneSameInstant(UTC).toLocalDateTime();
			}
			postBody.set(useLocalDates ? "localEndDate" : "endDate", isMidnight(dt) ? dt.toLocalDate() : dt);
		}
		if (metadataFilter != null && !metadataFilter.isBlank()) {
			postBody.set("metadataFilter", metadataFilter);
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
