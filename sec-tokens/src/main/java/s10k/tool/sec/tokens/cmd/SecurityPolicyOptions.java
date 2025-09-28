package s10k.tool.sec.tokens.cmd;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashSet;
import java.util.List;

import net.solarnetwork.domain.BasicSecurityPolicy;
import net.solarnetwork.domain.LocationPrecision;
import net.solarnetwork.domain.SecurityPolicy;
import net.solarnetwork.domain.datum.Aggregation;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;
import s10k.tool.common.util.DateUtils;
import s10k.tool.common.util.LocalDateTimeConverter;

/**
 * Security policy options.
 */
class SecurityPolicyOptions {

	// @formatter:off
	@Option(names = { "-node", "--node-id" },
			description = "a node ID to restrict access to",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "nodeId")
	Long[] nodeIds;

	@Option(names = { "-source", "--source-id" },
			description = "a source ID pattern to restrict access to",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "sourceId")
	String[] sourceIds;
	
	@ArgGroup(exclusive = true, multiplicity = "0..1")
	AggregationOptions aggregationOptions;
	
	@ArgGroup(exclusive = true, multiplicity = "0..1")
	LocationPrecisionOptions locationPrecisionOptions;
	
	@Option(names = { "-N", "--node-metadata-path" },
			description = "a node metadata path to restrict access to",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "metaPath")
	String[] nodeMetadataPaths;
	
	@Option(names = { "-U", "--user-metadata-path" },
			description = "a user metadata path to restrict access to",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "metaPath")
	String[] userMetadataPaths;
	
	@Option(names = { "-A", "--api-path" },
			description = "an API path to restrict access to",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "path")
	String[] apiPaths;
	
	@Option(names = { "-exp", "--expiration-date" },
			description = "an expiration date",
			converter = LocalDateTimeConverter.class)
	LocalDateTime expirationDate;

	@Option(names = {"-r", "--refresh-allowed"},
			description = "allow a token signing key to be refreshed")
	Boolean refreshAllowed;

	@Option(names = { "-tz", "--time-zone" },
			description = "a time zone to interpret date options as, instead of the local time zone")
	ZoneId zone;

	static class AggregationOptions {
		
		@Option(names = { "-agg", "--min-aggregation" },
				description = "a minimum aggregation to restrict access to",
				paramLabel = "aggregation")
		Aggregation minAggregations;

		@Option(names = { "--aggregation" },
				description = "an aggregation to restrict access to",
				split = "\\s*,\\s*",
				splitSynopsisLabel = ",",
				paramLabel = "aggregation")
		Aggregation[] aggregations;
		
	}
	
	static class LocationPrecisionOptions {
		
		@Option(names = { "-loc", "--min-location-precision" },
				description = "a minimum location precision to restrict access to",
				paramLabel = "locationPrecision")
		LocationPrecision minLocationPrecision;

		@Option(names = { "--location-precision" },
				description = "an location precision to restrict access to",
				split = "\\s*,\\s*",
				splitSynopsisLabel = ",",
				paramLabel = "locationPrecision")
		LocationPrecision[] locationPrecisions;
		
	}
	// @formatter:on

	/**
	 * Create a security policy out of the configured options.
	 * 
	 * @return the policy instance, or {@code null} if no policy options configured
	 */
	SecurityPolicy toPolicy() {
		if ((nodeIds == null || nodeIds.length < 1) && (sourceIds == null || sourceIds.length < 1)
				&& aggregationOptions == null && locationPrecisionOptions == null
				&& (nodeMetadataPaths == null || nodeMetadataPaths.length < 1)
				&& (userMetadataPaths == null || userMetadataPaths.length < 1)
				&& (apiPaths == null || apiPaths.length < 1) && expirationDate == null && refreshAllowed == null) {
			return null;
		}
		// @formatter:off
		BasicSecurityPolicy.Builder builder = BasicSecurityPolicy.builder()
				.withNodeIds(nodeIds != null ? new LinkedHashSet<>(List.of(nodeIds)) : null)
				.withSourceIds(sourceIds != null ? new LinkedHashSet<>(List.of(sourceIds)) : null)
				.withNodeMetadataPaths(nodeMetadataPaths != null ? new LinkedHashSet<>(List.of(nodeMetadataPaths)) : null)
				.withUserMetadataPaths(userMetadataPaths != null ? new LinkedHashSet<>(List.of(userMetadataPaths)) : null)
				.withApiPaths(apiPaths != null ? new LinkedHashSet<>(List.of(apiPaths)) : null)
				.withNotAfter(expirationDate != null ? DateUtils.zonedDate(expirationDate, zone).toInstant() : null)
				.withRefreshAllowed(refreshAllowed)
				;
		// @formatter:on
		if (aggregationOptions != null) {
			if (aggregationOptions.minAggregations != null) {
				builder.withMinAggregation(aggregationOptions.minAggregations);
			} else if (aggregationOptions.aggregations != null) {
				builder.withAggregations(new LinkedHashSet<>(List.of(aggregationOptions.aggregations)));
			}
		}
		if (locationPrecisionOptions != null) {
			if (locationPrecisionOptions.minLocationPrecision != null) {
				builder.withMinLocationPrecision(locationPrecisionOptions.minLocationPrecision);
			} else if (locationPrecisionOptions.locationPrecisions != null) {
				builder.withLocationPrecisions(
						new LinkedHashSet<>(List.of(locationPrecisionOptions.locationPrecisions)));
			}
		}
		return builder.build();
	}

}
