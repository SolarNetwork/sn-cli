package s10k.tool.datum.imp.domain;

import static net.solarnetwork.util.StringUtils.commaDelimitedStringFromCollection;

import java.util.SequencedCollection;

import org.jspecify.annotations.Nullable;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import net.solarnetwork.domain.SimplePagination;

/**
 * A mutable search filter for datum import entities.
 */
public class DatumImportsFilter extends SimplePagination {

	private @Nullable SequencedCollection<Long> jobIds;
	private @Nullable SequencedCollection<DatumImportState> jobStates;

	/**
	 * Constructor.
	 */
	public DatumImportsFilter() {
		super();
	}

	/**
	 * Get a multi-value map from this filter.
	 * 
	 * @return the multi-value map, suitable for using as request parameters
	 */
	public MultiValueMap<String, Object> toRequestMap() {
		var postBody = new LinkedMultiValueMap<String, Object>(4);
		if (jobIds != null && !jobIds.isEmpty()) {
			postBody.set("jobIds", commaDelimitedStringFromCollection(jobIds));
		}
		if (jobStates != null && !jobStates.isEmpty()) {
			postBody.set("states", commaDelimitedStringFromCollection(jobStates));
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
	 * Get the job IDs.
	 * 
	 * @return the jobIds
	 */
	public final @Nullable SequencedCollection<Long> getJobIds() {
		return jobIds;
	}

	/**
	 * Set the job IDs.
	 * 
	 * @param jobIds the job IDs to set
	 */
	public final void setJobIds(@Nullable SequencedCollection<Long> jobIds) {
		this.jobIds = jobIds;
	}

	/**
	 * Get the job states.
	 * 
	 * @return the job states
	 */
	public final @Nullable SequencedCollection<DatumImportState> getJobStates() {
		return jobStates;
	}

	/**
	 * Set the job states.
	 * 
	 * @param jobStates the job states to set
	 */
	public final void setJobStates(@Nullable SequencedCollection<DatumImportState> jobStates) {
		this.jobStates = jobStates;
	}

}
