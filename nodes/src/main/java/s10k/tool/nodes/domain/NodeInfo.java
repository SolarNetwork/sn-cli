package s10k.tool.nodes.domain;

import java.time.Instant;

import net.solarnetwork.domain.Location;

/**
 * Information about a node.
 */
public record NodeInfo(Long nodeId, Instant created, boolean requiresAuthorization, Long ownerId, String ownerEmail,
		Location location) {

}
