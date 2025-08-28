package s10k.tool.nodes.domain;

import java.time.Instant;

import net.solarnetwork.domain.datum.GeneralDatumMetadata;

/**
 * Node specific metadata.
 */
public record NodeMetadata(Long nodeId, Instant created, Instant updated, GeneralDatumMetadata metadata) {

}
