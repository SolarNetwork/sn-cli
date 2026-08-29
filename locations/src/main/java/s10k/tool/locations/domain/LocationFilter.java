package s10k.tool.locations.domain;

import static net.solarnetwork.util.StringUtils.commaDelimitedStringFromCollection;
import static s10k.tool.common.util.StringUtils.orderByList;

import java.util.List;
import java.util.SequencedCollection;

import org.jspecify.annotations.Nullable;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import net.solarnetwork.domain.SimpleLocation;
import net.solarnetwork.domain.SimplePagination;

/**
 * A mutable search filter for location related entities.
 */
public class LocationFilter extends SimplePagination {

	private @Nullable SimpleLocation location;
	private @Nullable SequencedCollection<LocationRequestStatus> requestStatuses;

	/**
	 * Constructor.
	 */
	public LocationFilter() {
		super();
	}

	/**
	 * Test if any criteria are configured on this instance.
	 * 
	 * @return {@code true} if at least one criteria is given
	 */
	public boolean hasCriteria() {
		var map = requestCriteria();
		return !map.isEmpty();
	}

	private MultiValueMap<String, Object> requestCriteria() {
		var postBody = new LinkedMultiValueMap<String, Object>(4);

		if (location != null) {
			if (location.getName() != null && !location.getName().isEmpty()) {
				postBody.set("location.name", location.getName());
			}
			if (location.getRegion() != null && !location.getRegion().isEmpty()) {
				postBody.set("location.region", location.getRegion());
			}
			if (location.getStateOrProvince() != null && !location.getStateOrProvince().isEmpty()) {
				postBody.set("location.stateOrProvince", location.getStateOrProvince());
			}
			if (location.getPostalCode() != null && !location.getPostalCode().isEmpty()) {
				postBody.set("location.postalCode", location.getPostalCode());
			}
			if (location.getCountry() != null && !location.getCountry().isEmpty()) {
				postBody.set("location.country", location.getCountry());
			}
			if (location.getTimeZoneId() != null && !location.getTimeZoneId().isEmpty()) {
				postBody.set("location.timeZoneId", location.getTimeZoneId());
			}
		}

		if (requestStatuses != null && !requestStatuses.isEmpty()) {
			postBody.set("requestStatuses", commaDelimitedStringFromCollection(requestStatuses));
		}

		return postBody;
	}

	/**
	 * Get a multi-value map from this filter.
	 * 
	 * @return the multi-value map, suitable for using as request parameters
	 */
	public MultiValueMap<String, Object> toRequestMap() {
		var postBody = requestCriteria();
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
	 * Get the location.
	 * 
	 * @return the location
	 */
	public final @Nullable SimpleLocation getLocation() {
		return location;
	}

	/**
	 * Set the location.
	 * 
	 * @param location the location to set
	 */
	public final void setLocation(@Nullable SimpleLocation location) {
		this.location = location;
	}

	/**
	 * Get the first location request status.
	 *
	 * <p>
	 * This returns the first available state from the
	 * {@link #getLocationRequestStatuses()} list, or {@code null} if not available.
	 * </p>
	 *
	 * @return the first status, or {@code null} if not available
	 */
	public final @Nullable LocationRequestStatus getLocationRequestStatus() {
		final SequencedCollection<LocationRequestStatus> array = getLocationRequestStatuses();
		return (array != null && !array.isEmpty() ? array.getFirst() : null);
	}

	/**
	 * Set the claimable job state.
	 *
	 * @param state the status to set
	 */
	public void setLocationRequestStatus(@Nullable LocationRequestStatus state) {
		setLocationRequestStatuses(state != null ? List.of(state) : null);
	}

	/**
	 * Get the location request statuses.
	 * 
	 * @return the statuses
	 */
	public final @Nullable SequencedCollection<LocationRequestStatus> getLocationRequestStatuses() {
		return requestStatuses;
	}

	/**
	 * Set the location request statuses.
	 *
	 * @param requestStatuses the statuses to set
	 */
	public final void setLocationRequestStatuses(@Nullable SequencedCollection<LocationRequestStatus> requestStatuses) {
		this.requestStatuses = requestStatuses;
	}

}
