package s10k.tool.datum.imp.domain;

import static s10k.tool.common.util.DateUtils.nonEpochInstant;

import java.time.Instant;

import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

import com.fasterxml.jackson.annotation.JsonProperty;

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
	 * Test if the job is in an importing state.
	 * 
	 * @return {@code true} if the state is <b>not</b> completed, staged, or
	 *         retracted
	 */
	public boolean isImporting() {
		return !(jobState == DatumImportState.Completed || jobState == DatumImportState.Staged
				|| jobState == DatumImportState.Retracted);
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
