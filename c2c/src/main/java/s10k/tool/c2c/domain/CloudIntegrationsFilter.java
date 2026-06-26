package s10k.tool.c2c.domain;

import static java.time.ZoneOffset.UTC;
import static net.solarnetwork.util.StringUtils.commaDelimitedStringFromCollection;
import static s10k.tool.common.util.DateUtils.isMidnight;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.SequencedCollection;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import net.solarnetwork.domain.SimplePagination;
import s10k.tool.common.domain.ClaimableJobState;

/**
 * A mutable search filter for cloud integration entities.
 */
public class CloudIntegrationsFilter extends SimplePagination {

	private SequencedCollection<Long> integrationIds;
	private SequencedCollection<Long> datumStreamIds;
	private SequencedCollection<Long> datumStreamMappingIds;
	private SequencedCollection<Long> taskIds;
	private SequencedCollection<Integer> indexes;
	private SequencedCollection<ClaimableJobState> claimableJobStates;
	private SequencedCollection<String> serviceIdentifiers;
	private SequencedCollection<Long> nodeIds;
	private ZonedDateTime startDate;
	private ZonedDateTime endDate;
	private Boolean enabled;

	/**
	 * Constructor.
	 */
	public CloudIntegrationsFilter() {
		super();
	}

	/**
	 * Get a multi-value map from this filter.
	 * 
	 * @return the multi-value map, suitable for using as request parameters
	 */
	public MultiValueMap<String, Object> toRequestMap() {
		var postBody = new LinkedMultiValueMap<String, Object>(4);
		if (integrationIds != null && !integrationIds.isEmpty()) {
			postBody.set("integrationIds", commaDelimitedStringFromCollection(integrationIds));
		}
		if (datumStreamIds != null && !datumStreamIds.isEmpty()) {
			postBody.set("datumStreamIds", commaDelimitedStringFromCollection(datumStreamIds));
		}
		if (datumStreamMappingIds != null && !datumStreamMappingIds.isEmpty()) {
			postBody.set("datumStreamMappingIds", commaDelimitedStringFromCollection(datumStreamMappingIds));
		}
		if (taskIds != null && !taskIds.isEmpty()) {
			postBody.set("taskIds", commaDelimitedStringFromCollection(taskIds));
		}
		if (indexes != null && !indexes.isEmpty()) {
			postBody.set("indexes", commaDelimitedStringFromCollection(indexes));
		}
		if (claimableJobStates != null && !claimableJobStates.isEmpty()) {
			postBody.set("claimableJobStates", commaDelimitedStringFromCollection(claimableJobStates));
		}
		if (serviceIdentifiers != null && !serviceIdentifiers.isEmpty()) {
			postBody.set("serviceIdentifiers", commaDelimitedStringFromCollection(serviceIdentifiers));
		}
		if (nodeIds != null && !nodeIds.isEmpty()) {
			postBody.set("nodeIds", commaDelimitedStringFromCollection(nodeIds));
		}
		if (startDate != null) {
			LocalDateTime utcDate = startDate.withZoneSameInstant(UTC).toLocalDateTime();
			postBody.set("startDate", isMidnight(utcDate) ? utcDate.toLocalDate() : utcDate);
		}
		if (endDate != null) {
			LocalDateTime utcDate = endDate.withZoneSameInstant(UTC).toLocalDateTime();
			postBody.set("endDate", isMidnight(utcDate) ? utcDate.toLocalDate() : utcDate);
		}
		if (enabled != null) {
			postBody.set("enabled", enabled);
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
	 * Get the first integration ID.
	 *
	 * <p>
	 * This returns the first available integration ID from the
	 * {@link #getIntegrationIds()} array, or {@code null} if not available.
	 * </p>
	 *
	 * @return the first ID, or {@code null} if not available
	 */
	public final Long getIntegrationId() {
		final SequencedCollection<Long> array = getIntegrationIds();
		return (array != null && !array.isEmpty() ? array.getFirst() : null);
	}

	/**
	 * Set the integration ID.
	 *
	 * @param integrationId the integration ID to set
	 */
	public final void setIntegrationId(Long integrationId) {
		setIntegrationIds(integrationId != null ? List.of(integrationId) : null);
	}

	/**
	 * Get the integration IDs.
	 * 
	 * @return the integration IDs
	 */
	public final SequencedCollection<Long> getIntegrationIds() {
		return integrationIds;
	}

	/**
	 * Set the integration IDs.
	 *
	 * @param integrationIds the integration IDs to set
	 */
	public final void setIntegrationIds(SequencedCollection<Long> integrationIds) {
		this.integrationIds = integrationIds;
	}

	/**
	 * Get the first datum stream ID.
	 *
	 * <p>
	 * This returns the first available datum stream ID from the
	 * {@link #getDatumStreamIds()} array, or {@code null} if not available.
	 * </p>
	 *
	 * @return the first ID, or {@code null} if not available
	 */
	public final Long getDatumStreamId() {
		final SequencedCollection<Long> array = getDatumStreamIds();
		return (array != null && !array.isEmpty() ? array.getFirst() : null);
	}

	/**
	 * Set the datum stream ID.
	 *
	 * @param datumStreamId the datum stream ID to set
	 */
	public final void setDatumStreamId(Long datumStreamId) {
		setDatumStreamIds(datumStreamId != null ? List.of(datumStreamId) : null);
	}

	/**
	 * Get the datum stream IDs.
	 * 
	 * @return the datum stream IDs
	 */
	public final SequencedCollection<Long> getDatumStreamIds() {
		return datumStreamIds;
	}

	/**
	 * Set the datum stream IDs.
	 *
	 * @param datumStreamIds the datum stream IDs to set
	 */
	public final void setDatumStreamIds(SequencedCollection<Long> datumStreamIds) {
		this.datumStreamIds = datumStreamIds;
	}

	/**
	 * Get the first datum stream mapping ID.
	 *
	 * <p>
	 * This returns the first available datum stream ID from the
	 * {@link #getDatumStreamMappingIds()} array, or {@code null} if not available.
	 * </p>
	 *
	 * @return the first ID, or {@code null} if not available
	 */
	public final Long getDatumStreamMappingId() {
		final SequencedCollection<Long> array = getDatumStreamMappingIds();
		return (array != null && !array.isEmpty() ? array.getFirst() : null);
	}

	/**
	 * Set the datum stream mapping ID.
	 *
	 * @param datumStreamMappingId the datum stream mapping ID to set
	 */
	public final void setDatumStreamMappingId(Long datumStreamMappingId) {
		setDatumStreamMappingIds(datumStreamMappingId != null ? List.of(datumStreamMappingId) : null);
	}

	/**
	 * Get the datum stream mapping IDs.
	 * 
	 * @return the IDs
	 */
	public final SequencedCollection<Long> getDatumStreamMappingIds() {
		return datumStreamMappingIds;
	}

	/**
	 * Set the datum stream mapping IDs.
	 *
	 * @param datumStreamMappingIds the datum stream mapping IDs to set
	 */
	public final void setDatumStreamMappingIds(SequencedCollection<Long> datumStreamMappingIds) {
		this.datumStreamMappingIds = datumStreamMappingIds;
	}

	/**
	 * Get the first index.
	 *
	 * <p>
	 * This returns the first available index from the {@link #getIndexes()} array,
	 * or {@code null} if not available.
	 * </p>
	 *
	 * @return the first index, or {@code null} if not available
	 */
	public final Integer getIndex() {
		final SequencedCollection<Integer> array = getIndexes();
		return (array != null && !array.isEmpty() ? array.getFirst() : null);
	}

	/**
	 * Set the index.
	 *
	 * @param index the index to set
	 */
	public final void setIndex(Integer index) {
		setIndexes(index != null ? List.of(index) : null);
	}

	/**
	 * Get the indexes.
	 *
	 * @return the indexes
	 */
	public final SequencedCollection<Integer> getIndexes() {
		return indexes;
	}

	/**
	 * Set the indexes.
	 *
	 * @param indexes the indexes to set
	 */
	public final void setIndexes(SequencedCollection<Integer> indexes) {
		this.indexes = indexes;
	}

	/**
	 * Get the first state.
	 *
	 * <p>
	 * This returns the first available state from the
	 * {@link #getClaimableJobStates()} array, or {@code null} if not available.
	 * </p>
	 *
	 * @return the first state, or {@code null} if not available
	 */
	public ClaimableJobState getClaimableJobState() {
		final SequencedCollection<ClaimableJobState> array = getClaimableJobStates();
		return (array != null && !array.isEmpty() ? array.getFirst() : null);
	}

	/**
	 * Set the claimable job state.
	 *
	 * @param state the state to set
	 */
	public void setClaimableJobState(ClaimableJobState state) {
		setClaimableJobStates(state != null ? List.of(state) : null);
	}

	/**
	 * Get the claimable job states.
	 * 
	 * @return the state
	 */
	public final SequencedCollection<ClaimableJobState> getClaimableJobStates() {
		return claimableJobStates;
	}

	/**
	 * Set the claimable job states.
	 *
	 * @param claimableJobStates the states to set
	 */
	public final void setClaimableJobStates(SequencedCollection<ClaimableJobState> claimableJobStates) {
		this.claimableJobStates = claimableJobStates;
	}

	/**
	 * Get the start date.
	 * 
	 * @return the start date
	 */
	public final ZonedDateTime getStartDate() {
		return startDate;
	}

	/**
	 * Set the start date.
	 *
	 * @param startDate the date to set
	 */
	public final void setStartDate(ZonedDateTime startDate) {
		this.startDate = startDate;
	}

	/**
	 * Get the end date.
	 * 
	 * @return the end date
	 */
	public final ZonedDateTime getEndDate() {
		return endDate;
	}

	/**
	 * Set the end date.
	 *
	 * @param endDate the date to set
	 */
	public final void setEndDate(ZonedDateTime endDate) {
		this.endDate = endDate;
	}

	/**
	 * Get the first service identifier.
	 *
	 * <p>
	 * This returns the first available service identifier from the
	 * {@link #getServiceIdentifiers()} array, or {@code null} if not available.
	 * </p>
	 *
	 * @return the first service identifier, or {@code null} if not available
	 */
	public String getServiceIdentifier() {
		final SequencedCollection<String> array = getServiceIdentifiers();
		return (array != null && !array.isEmpty() ? array.getFirst() : null);
	}

	/**
	 * Set the service identifier.
	 *
	 * @param serviceIdentifier the identifier to set
	 */
	public void setServiceIdentifier(String serviceIdentifier) {
		setServiceIdentifiers(serviceIdentifiers != null ? List.of(serviceIdentifier) : null);
	}

	/**
	 * Get the service identifiers.
	 * 
	 * @return service identifiers
	 */
	public final SequencedCollection<String> getServiceIdentifiers() {
		return serviceIdentifiers;
	}

	/**
	 * Set the service identifiers.
	 *
	 * @param serviceIdentifiers the service identifiers to set
	 */
	public final void setServiceIdentifiers(SequencedCollection<String> serviceIdentifiers) {
		this.serviceIdentifiers = serviceIdentifiers;
	}

	/**
	 * Get the first task ID.
	 *
	 * <p>
	 * This returns the first available task ID from the {@link #getTaskIds()}
	 * array, or {@code null} if not available.
	 * </p>
	 *
	 * @return the first task ID, or {@code null} if not available
	 */
	public final Long getTaskId() {
		final SequencedCollection<Long> array = getTaskIds();
		return (array != null && !array.isEmpty() ? array.getFirst() : null);
	}

	/**
	 * Set the task ID.
	 *
	 * @param taskId the task ID to set
	 */
	public final void setTaskId(Long taskId) {
		setTaskIds(taskId != null ? List.of(taskId) : null);
	}

	/**
	 * Get the task IDs.
	 * 
	 * @return the task IDs
	 */
	public final SequencedCollection<Long> getTaskIds() {
		return taskIds;
	}

	/**
	 * Set the task IDs.
	 *
	 * @param taskIds the task IDs to set
	 */
	public final void setTaskIds(SequencedCollection<Long> taskIds) {
		this.taskIds = taskIds;
	}

	/**
	 * Get the first node ID.
	 *
	 * <p>
	 * This returns the first available node ID from the {@link #getNodeIds()}
	 * array, or {@code null} if not available.
	 * </p>
	 *
	 * @return the first ID, or {@code null} if not available
	 */
	public final Long getNodeId() {
		final SequencedCollection<Long> array = getNodeIds();
		return (array != null && !array.isEmpty() ? array.getFirst() : null);
	}

	/**
	 * Set the node ID.
	 *
	 * @param nodeId the node ID to set
	 */
	public final void setNodeId(Long nodeId) {
		setNodeIds(nodeId != null ? List.of(nodeId) : null);
	}

	/**
	 * Get the node IDs.
	 * 
	 * @return the node IDs
	 */
	public final SequencedCollection<Long> getNodeIds() {
		return nodeIds;
	}

	/**
	 * Set the node IDs.
	 *
	 * @param nodeIds the node IDs to set
	 */
	public final void setNodeIds(SequencedCollection<Long> nodeIds) {
		this.nodeIds = nodeIds;
	}

	/**
	 * Get the enabled flag.
	 * 
	 * @return the enabled flag
	 */
	public final Boolean getEnabled() {
		return enabled;
	}

	/**
	 * Set the enabled flag.
	 *
	 * @param enabled the enabled to set
	 */
	public final void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

}
