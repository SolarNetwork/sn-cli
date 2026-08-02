package s10k.tool.nodes.domain;

import static net.solarnetwork.util.StringUtils.commaDelimitedStringFromCollection;
import static s10k.tool.common.util.StringUtils.orderByList;

import java.util.ArrayList;
import java.util.SequencedCollection;

import org.jspecify.annotations.Nullable;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.annotation.JsonDeserializeAs;

import net.solarnetwork.domain.SimpleLocation;
import net.solarnetwork.domain.SimplePagination;

/**
 * A mutable search filter for node related entities.
 */
public class NodeFilter extends SimplePagination {

	private @Nullable SequencedCollection<Long> locationIds;
	private @Nullable SequencedCollection<Long> nodeIds;
	private @Nullable SequencedCollection<String> names;
	private @Nullable SimpleLocation location;
	private boolean withoutTotalResultsCount = true;

	/**
	 * Default constructor.
	 */
	public NodeFilter() {
		super();
	}

	/**
	 * Get a multi-value map from this filter.
	 * 
	 * @return the multi-value map, suitable for using as request parameters
	 */
	public MultiValueMap<String, Object> toRequestMap() {
		var postBody = new LinkedMultiValueMap<String, Object>(4);
		if (locationIds != null && !locationIds.isEmpty()) {
			postBody.set("locationIds", commaDelimitedStringFromCollection(locationIds));
		}
		if (nodeIds != null && !nodeIds.isEmpty()) {
			postBody.set("nodeIds", commaDelimitedStringFromCollection(nodeIds));
		}
		if (names != null && !names.isEmpty()) {
			postBody.set("names", commaDelimitedStringFromCollection(names));
		}

		if (location != null) {
			if (location.getCountry() != null && !location.getCountry().isEmpty()) {
				postBody.set("location.country", location.getCountry());
			}
			if (location.getTimeZoneId() != null && !location.getTimeZoneId().isEmpty()) {
				postBody.set("location.timeZoneId", location.getTimeZoneId());
			}
		}
		postBody.set("withoutTotalResultsCount", withoutTotalResultsCount);
		if (getSorts() != null && !getSorts().isEmpty()) {
			postBody.set("orderBy", commaDelimitedStringFromCollection(orderByList(getSorts())));
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
	 * Get the location IDs
	 * 
	 * @return the location IDs
	 */
	public @Nullable SequencedCollection<Long> getLocationIds() {
		return locationIds;
	}

	/**
	 * Set the location IDs.
	 * 
	 * @param locationIds the location IDs to set
	 */
	@JsonDeserializeAs(value = ArrayList.class)
	public void setLocationIds(@Nullable SequencedCollection<Long> locationIds) {
		this.locationIds = locationIds;
	}

	/**
	 * Get the node IDs.
	 * 
	 * @return the node IDs
	 */
	public @Nullable SequencedCollection<Long> getNodeIds() {
		return nodeIds;
	}

	/**
	 * Set the node IDs.
	 * 
	 * @param nodeIds the node IDs to set
	 */
	@JsonDeserializeAs(value = ArrayList.class)
	public void setNodeIds(@Nullable SequencedCollection<Long> nodeIds) {
		this.nodeIds = nodeIds;
	}

	/**
	 * Get the names.
	 * 
	 * @return the names
	 */
	public final SequencedCollection<String> getNames() {
		return names;
	}

	/**
	 * Set the names.
	 * 
	 * @param names the names to set
	 */
	@JsonDeserializeAs(value = ArrayList.class)
	public final void setNames(SequencedCollection<String> names) {
		this.names = names;
	}

	/**
	 * Get the location.
	 * 
	 * @return the location
	 */
	public final SimpleLocation getLocation() {
		return location;
	}

	/**
	 * Set the location.
	 * 
	 * @param location the location to set
	 */
	public final void setLocation(SimpleLocation location) {
		this.location = location;
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

}
