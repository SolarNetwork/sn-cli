package s10k.tool.nodes.domain;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

/**
 * Information on a node certificate.
 */
@RegisterReflectionForBinding
public record NodeCertificateInfo(Long userId, Long nodeId, Long serialNumber, String issuerDN, String subjectDN,
		Instant validFromDate, Instant validUntilDate, Instant renewAfterDate, String status) {

	/**
	 * Get an instance for an invalid certificate password.
	 * 
	 * @param nodeId the node ID
	 * @return the instance
	 */
	public static NodeCertificateInfo invalidPassword(Long nodeId) {
		return new NodeCertificateInfo(null, nodeId, null, null, null, null, null, null, "Bad password");
	}

	/**
	 * Get an instance for an missing certificate password.
	 * 
	 * @param nodeId the node ID
	 * @return the instance
	 */
	public static NodeCertificateInfo missingPassword(Long nodeId) {
		return new NodeCertificateInfo(null, nodeId, null, null, null, null, null, null, "Missing password");
	}

	/**
	 * Get an instance for when the certificate view request was denied.
	 * 
	 * @param nodeId the node ID
	 * @return the instance
	 */
	public static NodeCertificateInfo forbidden(Long nodeId) {
		return new NodeCertificateInfo(null, nodeId, null, null, null, null, null, null, "Unavailable");
	}

	/**
	 * Get an instance for when the certificate view request resulted in an error.
	 * 
	 * @param nodeId the node ID
	 * @param msg    the message
	 * @return the instance
	 */
	public static NodeCertificateInfo error(Long nodeId, String msg) {
		return new NodeCertificateInfo(null, nodeId, null, null, null, null, null, null, "Error: " + msg);
	}

	/**
	 * Get the number of days between now and the renewal date.
	 * 
	 * @param zone the zone
	 * @return the number of days between now and the renewal date
	 */
	public long daysUntilRenewAfterDate(ZoneId zone) {
		if (renewAfterDate == null) {
			return 0;
		}
		ZoneId tz = (zone != null ? zone : ZoneId.systemDefault());
		final var now = ZonedDateTime.now(tz);
		if (renewAfterDate.isBefore(now.toInstant())) {
			return 0;
		}
		var dt = renewAfterDate.atZone(zone);
		return ChronoUnit.DAYS.between(now, dt);
	}

}
