package s10k.tool.user.events.domain;

import static java.time.ZoneOffset.UTC;
import static net.solarnetwork.util.StringUtils.commaDelimitedStringFromCollection;
import static s10k.tool.common.util.DateUtils.isMidnight;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.SequencedCollection;

import org.jspecify.annotations.Nullable;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.annotation.JsonDeserializeAs;
import com.fasterxml.jackson.annotation.JsonIgnore;

import net.solarnetwork.domain.SimplePagination;

/**
 * A mutable search filter for user event entities.
 */
public class UserEventsFilter extends SimplePagination {

	private @Nullable ZonedDateTime startDate;
	private @Nullable ZonedDateTime endDate;
	private @Nullable SequencedCollection<String> tags;
	private @Nullable String searchFilter;

	/**
	 * Constructor.
	 */
	public UserEventsFilter() {
		super();
	}

	/**
	 * Get a multi-value map from this filter.
	 * 
	 * @return the multi-value map, suitable for using as request parameters
	 */
	public MultiValueMap<String, Object> toRequestMap() {
		var postBody = new LinkedMultiValueMap<String, Object>(4);
		if (startDate != null) {
			LocalDateTime utcDate = startDate.withZoneSameInstant(UTC).toLocalDateTime();
			postBody.set("startDate", isMidnight(utcDate) ? utcDate.toLocalDate() : utcDate);
		}
		if (endDate != null) {
			LocalDateTime utcDate = endDate.withZoneSameInstant(UTC).toLocalDateTime();
			postBody.set("endDate", isMidnight(utcDate) ? utcDate.toLocalDate() : utcDate);
		}
		if (tags != null && !tags.isEmpty()) {
			postBody.set("tags", commaDelimitedStringFromCollection(tags));
		}
		if (searchFilter != null && !searchFilter.isEmpty()) {
			postBody.set("searchFilter", searchFilter);
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
	 * Test if the filter as an absolute date range specified.
	 *
	 * @return {@literal true} if both a start and end date are non-null
	 */
	@JsonIgnore
	public boolean hasDateRange() {
		return (getStartDate() != null && getEndDate() != null);
	}

	/**
	 * Get the start date.
	 * 
	 * @return the start date
	 */
	public @Nullable ZonedDateTime getStartDate() {
		return startDate;
	}

	/**
	 * Set the start date.
	 *
	 * @param startDate the date to set
	 */
	public void setStartDate(@Nullable ZonedDateTime startDate) {
		this.startDate = startDate;
	}

	/**
	 * Get the end date.
	 * 
	 * @return the end date
	 */
	public @Nullable ZonedDateTime getEndDate() {
		return endDate;
	}

	/**
	 * Set the end date.
	 *
	 * @param endDate the date to set
	 */
	public void setEndDate(@Nullable ZonedDateTime endDate) {
		this.endDate = endDate;
	}

	/**
	 * Get the tags.
	 * 
	 * @return the tags
	 */
	public final @Nullable SequencedCollection<String> getTags() {
		return tags;
	}

	/**
	 * Set the tags.
	 * 
	 * @param tags the tags to set
	 */
	@JsonDeserializeAs(value = ArrayList.class)
	public final void setTags(@Nullable SequencedCollection<String> tags) {
		this.tags = tags;
	}

	/**
	 * Get the search filter.
	 * 
	 * @return the search filter
	 */
	public final @Nullable String getSearchFilter() {
		return searchFilter;
	}

	/**
	 * Set the search filter.
	 * 
	 * @param searchFilter the search filter to set
	 */
	public final void setSearchFilter(@Nullable String searchFilter) {
		this.searchFilter = searchFilter;
	}

}
