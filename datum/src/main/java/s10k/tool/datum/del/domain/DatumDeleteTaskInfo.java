package s10k.tool.datum.del.domain;

import static s10k.tool.common.util.DateUtils.nonEpochInstant;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;

import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

import com.fasterxml.jackson.annotation.JsonProperty;

import s10k.tool.common.domain.ClaimableJobState;
import s10k.tool.datum.domain.DatumFilter;

/*-
{
    "id": "7d8a3ad7-0c2c-44ab-b708-6555296f4af3"
    "jobState": "Queued",
    "userId": 147,
    "configuration": {
      "nodeIds": [1,2,3]
      "sourceIds": ["/power/1","/power/2"],
      "aggregation": "None",
      "localStartDate": "2019-01-22 00:50:00",
      "localEndDate": "2020-01-25 04:00:00"
    },
    "percentComplete": 0.0,
    "submitDate": 1648002593507,
    "startedDate": 0,
    "completionDate": 0,
    "resultCount": 0,
    "success": false,
    "jobDuration": 0,
}
 */

/**
 * Delete task information.
 */
@RegisterReflectionForBinding
public record DatumDeleteTaskInfo(
// @formatter:off
		@JsonProperty("userId") Long userId,
		@JsonProperty("jobId") String jobId,
		@JsonProperty("jobState") ClaimableJobState jobState,
		@JsonProperty("success") boolean success,
		@JsonProperty("submitDate") Instant submitDate,
		@JsonProperty(value = "startedDate", required = false) @Nullable Instant startedDate,
		@JsonProperty(value = "completionDate", required = false) @Nullable Instant completionDate,
		@JsonProperty("resultCount") long resultCount,
		@JsonProperty("percentComplete") double percentComplete,
		@JsonProperty("jobDuration") Duration jobDuration,
		@JsonProperty("configuration") DatumFilter configuration
		// @formatter:on
) {

	/**
	 * Test if the job is in an importing state.
	 * 
	 * @return {@code true} if the state is <b>not</b> completed
	 */
	public boolean isDeleting() {
		return jobState != ClaimableJobState.Completed;
	}

	/**
	 * Normalize the info.
	 * 
	 * <p>
	 * The optional properties will be normalized to {@code null} as appropriate.
	 * </p>
	 * 
	 * @return the normalized info
	 */
	public DatumDeleteTaskInfo normalized() {
		// @formatter:off
		return new DatumDeleteTaskInfo(
				userId,
				jobId,
				jobState,
				success,
				submitDate,
				nonEpochInstant(startedDate),
				nonEpochInstant(completionDate),
				resultCount,
				percentComplete,
				jobDuration,
				configuration
			);
		// @formatter:on
	}

	/**
	 * Get a normalized filter configuration map.
	 * 
	 * @return the filter configuration as a map
	 */
	public Map<String, Object> filterConfiguration() {
		var result = configuration.toRequestMap().toSingleValueMap();
		result.remove("partialAggregation");
		return result;
	}

	/**
	 * Create a copy with a different state.
	 * 
	 * @param newState the desired state
	 * @return the copy
	 */
	public DatumDeleteTaskInfo copyWithState(ClaimableJobState newState) {
		return new DatumDeleteTaskInfo(userId, jobId, newState, success, submitDate, startedDate, completionDate,
				resultCount, percentComplete, jobDuration, configuration);
	}

	/**
	 * Create a copy with a different configuration.
	 * 
	 * @param newConfiguration the desired configuration
	 * @return the copy
	 */
	public DatumDeleteTaskInfo copyWithConfiguration(DatumFilter newConfiguration) {
		return new DatumDeleteTaskInfo(userId, jobId, jobState, success, submitDate, startedDate, completionDate,
				resultCount, percentComplete, jobDuration, newConfiguration);
	}

}
