package s10k.tool.locations.domain;

import static net.solarnetwork.util.StringUtils.commaDelimitedStringFromCollection;
import static s10k.tool.common.util.StringUtils.orderByList;

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

}
