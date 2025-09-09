package s10k.tool.datum.domain;

import static java.time.ZoneOffset.UTC;
import static net.solarnetwork.util.StringUtils.commaDelimitedStringFromCollection;
import static s10k.tool.common.util.DateUtils.isMidnight;
import static s10k.tool.common.util.StringUtils.parseStreamIdentifiers;

import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.SequencedCollection;
import java.util.Set;
import java.util.SortedSet;
import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import net.solarnetwork.domain.SimplePagination;
import net.solarnetwork.domain.datum.Aggregation;
import net.solarnetwork.domain.datum.CombiningType;
import net.solarnetwork.domain.datum.DatumReadingType;
import net.solarnetwork.domain.datum.DatumRollupType;
import net.solarnetwork.domain.datum.ObjectDatumKind;
import net.solarnetwork.util.StringUtils;

/**
 * A mutable search filter for datum.
 */
public final class DatumFilter extends SimplePagination {

	private ObjectDatumKind objectKind;
	private SequencedCollection<UUID> streamIds;
	private SequencedCollection<Long> objectIds;
	private SequencedCollection<String> sourceIds;
	private ZonedDateTime startDate;
	private ZonedDateTime endDate;
	private LocalDateTime localStartDate;
	private LocalDateTime localEndDate;
	private boolean mostRecent = false;
	private boolean withoutTotalResultsCount = true;
	private Aggregation aggregation;
	private Aggregation partialAggregation;
	private DatumReadingType readingType;
	private Period timeTolerance;
	private SequencedCollection<DatumRollupType> datumRollupTypes;
	private CombiningType combiningType;
	private Map<Long, Set<Long>> objectIdMappings;
	private Map<String, Set<String>> sourceIdMappings;

	/**
	 * Default constructor.
	 */
	public DatumFilter() {
		super();
	}

	/**
	 * Get a multi-value map from this filter.
	 * 
	 * @return the multi-value map, suitable for using as request parameters
	 */
	public MultiValueMap<String, Object> toRequestMap() {
		var postBody = new LinkedMultiValueMap<String, Object>(4);
		if (streamIds != null && !streamIds.isEmpty()) {
			postBody.set("streamIds", commaDelimitedStringFromCollection(streamIds));
		}
		if (objectIds != null && !objectIds.isEmpty()) {
			postBody.set((objectKind == ObjectDatumKind.Location ? "locationIds" : "nodeIds"),
					commaDelimitedStringFromCollection(objectIds));
		}
		if (sourceIds != null && !sourceIds.isEmpty()) {
			postBody.set("sourceIds", commaDelimitedStringFromCollection(sourceIds));
		}
		if (localStartDate != null && localEndDate != null) {
			postBody.set("localStartDate", isMidnight(localStartDate) ? localStartDate.toLocalDate() : localStartDate);
			postBody.set("localEndDate", isMidnight(localEndDate) ? localEndDate.toLocalDate() : localEndDate);
		} else {
			if (startDate != null) {
				LocalDateTime utcDate = startDate.withZoneSameInstant(UTC).toLocalDateTime();
				postBody.set("startDate", isMidnight(utcDate) ? utcDate.toLocalDate() : utcDate);
			}
			if (endDate != null) {
				LocalDateTime utcDate = endDate.withZoneSameInstant(UTC).toLocalDateTime();
				postBody.set("endDate", isMidnight(utcDate) ? utcDate.toLocalDate() : utcDate);
			}
		}
		if (mostRecent) {
			postBody.set("mostRecent", mostRecent);
		}
		if (aggregation != null) {
			postBody.set("aggregation", aggregation.name());
		}
		if (partialAggregation != null) {
			postBody.set("partialAggregation", partialAggregation.name());
		}
		if (combiningType != null) {
			postBody.set("combiningType", combiningType.name());
		}
		if (objectIdMappings != null && !objectIdMappings.isEmpty()) {
			for (Entry<Long, Set<Long>> mapping : objectIdMappings.entrySet()) {
				postBody.add((objectKind == ObjectDatumKind.Location ? "locationIdMaps" : "nodeIdMaps"),
						mappingsParameter(mapping.getKey(), mapping.getValue()));
			}
		}
		if (sourceIdMappings != null && !sourceIdMappings.isEmpty()) {
			for (Entry<String, Set<String>> mapping : sourceIdMappings.entrySet()) {
				postBody.add("sourceIdMaps", mappingsParameter(mapping.getKey(), mapping.getValue()));
			}
		}
		if (readingType != null) {
			postBody.set("readingType", readingType.name());
		}
		if (getMax() != null && getMax() > 0) {
			postBody.set("max", getMax());
		}
		if (getOffset() != null && getOffset() > 0) {
			postBody.set("offset", getOffset());
		}
		return postBody;
	}

	/**
	 * Set node and source IDs via a collection of stream identifiers.
	 * 
	 * @param identifiers the collection of identifiers, each like
	 *                    {@code OBJECT_ID:SOURCE_ID}
	 * @see s10k.tool.common.util.StringUtils#parseStreamIdentifiers(java.util.Collection)
	 */
	public void populateIdsFromStreamIdentifiers(SequencedCollection<String> identifiers) {
		NavigableMap<Long, SortedSet<String>> mappings = parseStreamIdentifiers(identifiers);
		setObjectIds(mappings.sequencedKeySet());
		setSourceIds(mappings.sequencedValues().stream().flatMap(c -> c.stream()).toList());
	}

	/**
	 * Test if the query style is for aggregate results.
	 * 
	 * @return {@code true} if aggregate results are returned
	 */
	public boolean isAggregateStyle() {
		return (aggregation != null && aggregation != Aggregation.None);
	}

	/**
	 * Test if the query style is for reading results.
	 * 
	 * @return {@code true} if reading results are returned
	 */
	public boolean isReadingStyle() {
		return (readingType != null);
	}

	/**
	 * Test if the query style is for a single "reading" record result.
	 * 
	 * @return {@code true} if a single reading record result is returned
	 */
	public boolean isReadingRecordStyle() {
		return (isReadingStyle() && !isAggregateStyle());
	}

	/**
	 * Test if the query style is for reading aggregate results.
	 * 
	 * @return {@code true} if reading aggregates results are returned
	 */
	public boolean isReadingAggregateStyle() {
		return (isReadingStyle() && isAggregateStyle());
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BasicDatumCriteria{");
		if (objectIds != null) {
			builder.append("objectIds=");
			builder.append(objectIds);
			builder.append(", ");
		}
		if (sourceIds != null) {
			builder.append("sourceIds=");
			builder.append(sourceIds);
			builder.append(", ");
		}
		if (streamIds != null) {
			builder.append("streamIds=");
			builder.append(streamIds);
			builder.append(", ");
		}
		if (startDate != null) {
			builder.append("startDate=");
			builder.append(startDate);
			builder.append(", ");
		}
		if (endDate != null) {
			builder.append("endDate=");
			builder.append(endDate);
			builder.append(", ");
		}
		if (localStartDate != null) {
			builder.append("localStartDate=");
			builder.append(localStartDate);
			builder.append(", ");
		}
		if (localEndDate != null) {
			builder.append("localEndDate=");
			builder.append(localEndDate);
			builder.append(", ");
		}
		if (mostRecent) {
			builder.append("mostRecent=true, ");
		}
		if (aggregation != null) {
			builder.append("aggregation=");
			builder.append(aggregation);
			builder.append(", ");
		}
		if (partialAggregation != null) {
			builder.append("partialAggregation=");
			builder.append(partialAggregation);
			builder.append(", ");
		}
		if (datumRollupTypes != null) {
			builder.append("datumRollupTypes=");
			builder.append(datumRollupTypes);
			builder.append(", ");
		}
		if (readingType != null) {
			builder.append("readingType=");
			builder.append(readingType);
			builder.append(", ");
		}
		builder.append("}");
		return builder.toString();
	}

	/**
	 * Get the object IDs
	 * 
	 * @return the object IDs
	 */
	public SequencedCollection<Long> getObjectIds() {
		return objectIds;
	}

	/**
	 * Set the object IDs.
	 * 
	 * @param objectIds the object IDs to set
	 */
	public void setObjectIds(SequencedCollection<Long> objectIds) {
		this.objectIds = objectIds;
	}

	/**
	 * Get the source IDs.
	 * 
	 * @return the source IDs
	 */
	public SequencedCollection<String> getSourceIds() {
		return sourceIds;
	}

	/**
	 * Set the source IDs.
	 * 
	 * @param sourceIds the source IDs to set
	 */
	public void setSourceIds(SequencedCollection<String> sourceIds) {
		this.sourceIds = sourceIds;
	}

	/**
	 * Get the start date.
	 * 
	 * @return the start date
	 */
	public ZonedDateTime getStartDate() {
		return startDate;
	}

	/**
	 * Set the start date.
	 *
	 * @param startDate the date to set
	 */
	public void setStartDate(ZonedDateTime startDate) {
		this.startDate = startDate;
	}

	/**
	 * Get the end date.
	 * 
	 * @return the end date
	 */
	public ZonedDateTime getEndDate() {
		return endDate;
	}

	/**
	 * Set the end date.
	 *
	 * @param endDate the date to set
	 */
	public void setEndDate(ZonedDateTime endDate) {
		this.endDate = endDate;
	}

	/**
	 * Get the local start date.
	 * 
	 * @return the local start date
	 */
	public LocalDateTime getLocalStartDate() {
		return localStartDate;
	}

	/**
	 * Set the local start date.
	 *
	 * @param localStartDate the date to set
	 */
	public void setLocalStartDate(LocalDateTime localStartDate) {
		this.localStartDate = localStartDate;
	}

	/**
	 * Get the local end date.
	 * 
	 * @return the local end date
	 */
	public LocalDateTime getLocalEndDate() {
		return localEndDate;
	}

	/**
	 * Set the local end date.
	 *
	 * @param localEndDate the date to set
	 */
	public void setLocalEndDate(LocalDateTime localEndDate) {
		this.localEndDate = localEndDate;
	}

	/**
	 * Get the first available stream ID.
	 * 
	 * @return the first stream ID
	 */
	public UUID getStreamId() {
		return (streamIds != null && !streamIds.isEmpty() ? streamIds.getFirst() : null);
	}

	/**
	 * Set a single stream ID.
	 *
	 * <p>
	 * This will completely replace any existing {@code streamIds} value with a new
	 * array with a single value.
	 * </p>
	 *
	 * @param streamId the stream ID to set
	 */
	@SuppressWarnings("InvalidParam")
	public void setStreamId(UUID streamId) {
		setStreamIds(streamId == null ? null : List.of(streamId));
	}

	/**
	 * Get the stream IDs.
	 * 
	 * @return the stream IDs
	 */
	public SequencedCollection<UUID> getStreamIds() {
		return streamIds;
	}

	/**
	 * Set the stream IDs.
	 *
	 * @param streamIds the location IDs to set
	 */
	public void setStreamIds(SequencedCollection<UUID> streamIds) {
		this.streamIds = streamIds;
	}

	/**
	 * Get the "most recent" mode.
	 * 
	 * @return the "most recent" mode
	 */
	public boolean isMostRecent() {
		return mostRecent;
	}

	/**
	 * Set the "most recent" flag.
	 *
	 * @param mostRecent the "most recent" flag to set
	 */
	public void setMostRecent(boolean mostRecent) {
		this.mostRecent = mostRecent;
	}

	/**
	 * Toggle the "without total results" mode.
	 *
	 * @param mode the mode to set
	 */
	public void setWithoutTotalResultsCount(boolean mode) {
		this.withoutTotalResultsCount = mode;
	}

	/**
	 * Get the "without total results" mode.
	 * 
	 * @return the mode
	 */
	public boolean isWithoutTotalResultsCount() {
		return withoutTotalResultsCount;
	}

	/**
	 * Get the aggregation.
	 * 
	 * @return the aggregation
	 */
	public Aggregation getAggregation() {
		return aggregation;
	}

	/**
	 * Set the aggregation level to use.
	 *
	 * @param aggregation the aggregation to set
	 */
	public void setAggregation(Aggregation aggregation) {
		this.aggregation = aggregation;
	}

	/**
	 * Get the partial aggregation.
	 * 
	 * @return the partial aggregation
	 */
	public Aggregation getPartialAggregation() {
		return partialAggregation;
	}

	/**
	 * Set the partial aggregation to include.
	 *
	 * @param partialAggregation the partialAggregation to set
	 */
	public void setPartialAggregation(Aggregation partialAggregation) {
		this.partialAggregation = partialAggregation;
	}

	/**
	 * Get a single datum rollup type.
	 * 
	 * @return the datum rollup type
	 */
	public DatumRollupType getDatumRollupType() {
		SequencedCollection<DatumRollupType> types = getDatumRollupTypes();
		return types != null && !types.isEmpty() ? types.getFirst() : null;
	}

	/**
	 * Set a single datum rollup type.
	 *
	 * <p>
	 * This is a convenience method for requests that use a single rollup type at a
	 * time. The type is still stored on the {@code datumRollupTypes} array, as the
	 * first value. Calling this method replaces any existing
	 * {@code datumRollupTypes} value with a new array containing just the value
	 * passed into this method.
	 * </p>
	 *
	 * @param type the type to set
	 */
	public void setDatumRollupType(DatumRollupType type) {
		setDatumRollupTypes(type == null ? null : List.of(type));
	}

	/**
	 * Get the datum rollup types.
	 * 
	 * @return the rollup types
	 */
	public SequencedCollection<DatumRollupType> getDatumRollupTypes() {
		return datumRollupTypes;
	}

	/**
	 * Set the datum rollup types.
	 *
	 * @param datumRollupTypes the types to set
	 */
	public void setDatumRollupTypes(SequencedCollection<DatumRollupType> datumRollupTypes) {
		this.datumRollupTypes = datumRollupTypes;
	}

	/**
	 * Get the reading type.
	 * 
	 * @return the reading type
	 */
	public DatumReadingType getReadingType() {
		return readingType;
	}

	/**
	 * Set the reading type.
	 *
	 * @param readingType the type to set
	 */
	public void setReadingType(DatumReadingType readingType) {
		this.readingType = readingType;
	}

	/**
	 * Get the time tolerance.
	 * 
	 * @return the period
	 */
	public Period getTimeTolerance() {
		return timeTolerance;
	}

	/**
	 * Set the time tolerance.
	 *
	 * @param timeTolerance the period to set
	 */
	public void setTimeTolerance(Period timeTolerance) {
		this.timeTolerance = timeTolerance;
	}

	/**
	 * Get the object kind.
	 * 
	 * @return the object kind
	 */
	public ObjectDatumKind getObjectKind() {
		return objectKind;
	}

	/**
	 * Set the object kind.
	 *
	 * @param objectKind the object kind to set
	 */
	public void setObjectKind(ObjectDatumKind objectKind) {
		this.objectKind = objectKind;
	}

	/**
	 * Get the combining type.
	 * 
	 * @return the combining type
	 */
	public CombiningType getCombiningType() {
		return combiningType;
	}

	/**
	 * Set the combining type.
	 *
	 * @param combiningType the type to set
	 */
	public void setCombiningType(CombiningType combiningType) {
		this.combiningType = combiningType;
	}

	/**
	 * Get the object ID mappings.
	 * 
	 * @return the object ID mappings
	 */
	public Map<Long, Set<Long>> getObjectIdMappings() {
		return objectIdMappings;
	}

	/**
	 * Set the object ID mappings.
	 *
	 * @param objectIdMappings the objectIdMappings to set
	 */
	public void setObjectIdMappings(Map<Long, Set<Long>> objectIdMappings) {
		this.objectIdMappings = objectIdMappings;
	}

	/**
	 * Set the object ID mappings as an encoded string array.
	 *
	 * @param mappings the mapping values
	 * @see ObjectMappingCriteria#mappingsFrom(String[])
	 */
	public void setObjectIdMaps(String[] mappings) {
		setObjectIdMappings(objectMappingsFrom(mappings));
	}

	/**
	 * Get the source ID mappings.
	 * 
	 * @return the source ID mappings
	 */
	public Map<String, Set<String>> getSourceIdMappings() {
		return sourceIdMappings;
	}

	/**
	 * Set the source ID mappings.
	 *
	 * @param sourceIdMappings the source ID mappings to set
	 */
	public void setSourceIdMappings(Map<String, Set<String>> sourceIdMappings) {
		this.sourceIdMappings = sourceIdMappings;
	}

	/**
	 * Set the source ID mappings as an encoded string array.
	 *
	 * @param mappings the mapping values
	 */
	public void setSourceIdMaps(String[] mappings) {
		setSourceIdMappings(sourceMappingsFrom(mappings));
	}

	/**
	 * Get a mappings entry as a query parameter value.
	 * 
	 * @param key    the mapping key
	 * @param values the mapping value
	 * @return the query parameter value
	 */
	public static String mappingsParameter(Object key, Set<?> values) {
		StringBuilder buf = new StringBuilder();
		buf.append(key.toString());
		buf.append(':');
		buf.append(commaDelimitedStringFromCollection(values));
		return buf.toString();
	}

	/**
	 * Create source ID mappings from a list of string encoded mappings.
	 *
	 * <p>
	 * Each mapping in {@code mappings} must be encoded as
	 * {@literal VIRT_SRC_ID:SRC_ID1,SRC_ID2,...}. That is, a virtual source ID
	 * followed by a colon followed by a comma-delimited list of real source IDs.
	 * </p>
	 * <p>
	 * A special case is handled when the mappings are such that the first includes
	 * the colon delimiter (e.g. {@literal V:A}), and the remaining values are
	 * simple strings (e.g. {@literal B, C, D}). In that case a single virtual
	 * source ID mapping is created.
	 * </p>
	 *
	 * @param mappings the mappings to decode
	 * @return the mappings, or {@literal null} if {@code mappings} is empty
	 */
	public static Map<String, Set<String>> sourceMappingsFrom(String[] mappings) {
		Map<String, Set<String>> result;
		if (mappings == null || mappings.length < 1) {
			result = null;
		} else {
			result = new LinkedHashMap<>(mappings.length);
			for (String map : mappings) {
				int vIdDelimIdx = map.indexOf(':');
				if (vIdDelimIdx < 1 && result.size() == 1) {
					// special case, when Spring maps single query param into 3 fields split on
					// comma like A:B, C, D
					try {
						result.get(result.keySet().iterator().next()).add(map);
					} catch (NumberFormatException e) {
						// ignore
					}
					continue;
				} else if (vIdDelimIdx < 1 || vIdDelimIdx + 1 >= map.length()) {
					continue;
				}
				String vId = map.substring(0, vIdDelimIdx);
				Set<String> rSourceIds = StringUtils.commaDelimitedStringToSet(map.substring(vIdDelimIdx + 1));
				result.put(vId, rSourceIds);
			}
		}
		return (result == null || result.isEmpty() ? null : result);
	}

	/**
	 * Create object ID mappings from a list of string encoded mappings.
	 *
	 * <p>
	 * Each mapping in {@code mappings} must be encoded as
	 * {@literal VIRT_OBJ_ID:OBJ_ID1,OBJ_ID2,...}. That is, a virtual object ID
	 * followed by a colon followed by a comma-delimited list of real object IDs.
	 * </p>
	 * <p>
	 * A special case is handled when the mappings are such that the first includes
	 * the colon delimiter (e.g. {@literal 1:2}), and the remaining values are
	 * simple strings (e.g. {@literal 3, 4, 5}). In that case a single virtual
	 * object ID mapping is created.
	 * </p>
	 *
	 * @param mappings the mappings to decode
	 * @return the mappings, or {@literal null} if {@code mappings} is empty
	 */
	public static Map<Long, Set<Long>> objectMappingsFrom(String[] mappings) {
		Map<Long, Set<Long>> result;
		if (mappings == null || mappings.length < 1) {
			result = null;
		} else {
			result = new LinkedHashMap<>(mappings.length);
			for (String map : mappings) {
				int vIdDelimIdx = map.indexOf(':');
				if (vIdDelimIdx < 1 && result.size() == 1) {
					// special case, when Spring maps single query param into 3 fields split on
					// comma like 1:2, 3, 4
					try {
						result.get(result.keySet().iterator().next()).add(Long.valueOf(map));
					} catch (NumberFormatException e) {
						// ignore
					}
					continue;
				} else if (vIdDelimIdx < 1 || vIdDelimIdx + 1 >= map.length()) {
					continue;
				}
				try {
					Long vId = Long.valueOf(map.substring(0, vIdDelimIdx));
					Set<String> rIds = StringUtils.commaDelimitedStringToSet(map.substring(vIdDelimIdx + 1));
					Set<Long> rNodeIds = new LinkedHashSet<>(rIds.size());
					for (String rId : rIds) {
						rNodeIds.add(Long.valueOf(rId));
					}
					result.put(vId, rNodeIds);
				} catch (NumberFormatException e) {
					// ignore and continue
				}
			}
		}
		return (result == null || result.isEmpty() ? null : result);
	}

}
