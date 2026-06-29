package s10k.tool.nodes.domain;

import java.time.Instant;

import net.solarnetwork.domain.Location;

/**
 * Information about a node.
 */
public record NodeInfo(Long nodeId, Instant created, boolean requiresAuthorization, Long ownerId, String ownerEmail,
		Location location) {

	/**
	 * Get the node time zone.
	 * 
	 * @return the time zone, falling back to {@code UTC} if not otherwise available
	 */
	public String timeZoneId() {
		return (location != null && location.getTimeZoneId() != null && !location.getTimeZoneId().isEmpty()
				? location.getTimeZoneId()
				: "UTC");
	}

}
