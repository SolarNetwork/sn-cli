package s10k.tool.datum.stale.domain;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

import net.solarnetwork.domain.datum.Aggregation;

/**
 * A stale node datum aggregate record.
 */
public record StaleNodeDatumAggregate(
// @formatter:off
		  @JsonProperty("kind") Aggregation kind
		, @JsonProperty("nodeId") Long nodeId
		, @JsonProperty("sourceId") String sourceId
		, @JsonProperty("startDate") Instant startDate
		// @formatter:on
) {

}
