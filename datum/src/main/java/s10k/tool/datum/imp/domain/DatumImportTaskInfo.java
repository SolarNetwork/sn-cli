package s10k.tool.datum.imp.domain;

import static s10k.tool.common.util.DateUtils.nonEpochInstant;

import java.time.Instant;

import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

import com.fasterxml.jackson.annotation.JsonProperty;

/*-

{
    "jobId": "7e426ffb-5928-420d-90c6-c7c0404c6da1",
    "jobState": "Staged",
    "groupKey": "4dff899d-0fee-47e1-8f74-a0e74941ac1f",
    "success": false,
    "cancelled": false,
    "done": false,
    "userId": 147,
    "submitDate": 1615929546543,
    "startedDate": 0,
    "completionDate": 0,
    "loadedCount": 0,
    "percentComplete": 0.0,
    "importDate": 1615929545362,
    "configuration": { ... }
  }

 */

/**
 * Import task information.
 */
@RegisterReflectionForBinding
public record DatumImportTaskInfo(
// @formatter:off
		@JsonProperty("userId") Long userId,
		@JsonProperty("jobId") String jobId,
		@JsonProperty("jobState") DatumImportState jobState,
		@JsonProperty("importDate") Instant importDate,
		@JsonProperty(value = "groupKey", required = false) @Nullable String groupKey,
		@JsonProperty("success") boolean success,
		@JsonProperty("submitDate") Instant submitDate,
		@JsonProperty(value = "startedDate", required = false) @Nullable Instant startedDate,
		@JsonProperty(value = "completionDate", required = false) @Nullable Instant completionDate,
		@JsonProperty("loadedCount") long loadedCount,
		@JsonProperty("percentComplete") double percentComplete,
		@JsonProperty("configuration") DatumImportConfiguration configuration
		// @formatter:on
) {

	/**
	 * Normalize the info.
	 * 
	 * <p>
	 * The optional properties will be normalized to {@code null} as appropriate.
	 * </p>
	 * 
	 * @return the normalized info
	 */
	public DatumImportTaskInfo normalized() {
		// @formatter:off
		return new DatumImportTaskInfo(
				userId,
				jobId,
				jobState,
				importDate,
				groupKey,
				success,
				submitDate,
				nonEpochInstant(startedDate),
				nonEpochInstant(completionDate),
				loadedCount,
				percentComplete,
				configuration
			);
		// @formatter:on
	}

	/**
	 * Create a copy with a different state.
	 * 
	 * @param newState the desired state
	 * @return the copy
	 */
	public DatumImportTaskInfo copyWithState(DatumImportState newState) {
		return new DatumImportTaskInfo(userId, jobId, newState, importDate, groupKey, success, submitDate, startedDate,
				completionDate, loadedCount, percentComplete, configuration);
	}

	/**
	 * Create a copy with a different configuration.
	 * 
	 * @param newConfiguration the desired configuration
	 * @return the copy
	 */
	public DatumImportTaskInfo copyWithConfiguration(DatumImportConfiguration newConfiguration) {
		return new DatumImportTaskInfo(userId, jobId, jobState, importDate, groupKey, success, submitDate, startedDate,
				completionDate, loadedCount, percentComplete, newConfiguration);
	}

}
