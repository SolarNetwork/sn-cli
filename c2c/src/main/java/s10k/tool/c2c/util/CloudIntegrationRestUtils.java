package s10k.tool.c2c.util;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static s10k.tool.c2c.util.CloudIntegrationsUtils.datumStreamServiceLocalizedName;
import static s10k.tool.common.util.RestUtils.checkSuccess;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import s10k.tool.c2c.domain.CloudDatumStreamConfiguration;
import s10k.tool.c2c.domain.CloudIntegrationsFilter;
import s10k.tool.common.util.RestUtils;

/**
 * REST utilities for Cloud Integration APIs.
 */
public final class CloudIntegrationRestUtils {

	private CloudIntegrationRestUtils() {
		// not available
	}

	/**
	 * List cloud datum streams.
	 * 
	 * @param restClient   the REST client
	 * @param objectMapper the object mapper
	 * @param filter       an optional filter
	 * @return the result
	 */
	public static List<CloudDatumStreamConfiguration> listCloudDatumStreams(RestClient restClient,
			ObjectMapper objectMapper, CloudIntegrationsFilter filter) {
		// @formatter:off
		JsonNode response = restClient.get()
			.uri(b -> {
				b.path("/solaruser/api/v1/sec/user/c2c/datum-streams");
				if (filter != null ) {
					RestUtils.populateQueryParameters(b, filter::toRequestMap);
				}
				return b.build();
			})
			.accept(MediaType.APPLICATION_JSON)
			.retrieve()
			.body(JsonNode.class)
			;		
		// @formatter:on

		checkSuccess(response);

		List<CloudDatumStreamConfiguration> result = new ArrayList<>(response.path("data").size());
		for (JsonNode node : response.path("data").path("results")) {
			CloudDatumStreamConfiguration conf;
			try {
				conf = objectMapper.treeToValue(node, CloudDatumStreamConfiguration.class);
			} catch (JsonProcessingException | IllegalArgumentException e) {
				throw new IllegalStateException("Error parsing cloud datum stream list response: " + e.getMessage(), e);
			}
			if (conf != null) {
				result.add(conf);
			}
		}
		return result;
	}

	/**
	 * Get a collection of Cloud Datum Stream entities for services that match a set
	 * of type filters.
	 * 
	 * @param restClient   the client to use
	 * @param objectMapper the mapper to use
	 * @param filter       the query filter
	 * @param typeFilters  optional include/exclude filters (prefix with {@code !}
	 *                     to exclude
	 * @return the matching datum streams
	 */
	public static SortedMap<Long, CloudDatumStreamConfiguration> datumStreamsOfType(RestClient restClient,
			ObjectMapper objectMapper, CloudIntegrationsFilter filter, String[] typeFilters) {
		// local cache of known service identifiers to exclude
		final Set<String> includeServiceIdents = new HashSet<>();
		final Set<String> excludeServiceIdents = new HashSet<>();

		// create lower-case copy of types for case-insensitive compare
		final Set<String> lcIncludeTypes = new HashSet<>();
		final Set<String> lcExcludeTypes = new HashSet<>();
		if (typeFilters != null) {
			for (String type : typeFilters) {
				if (type.startsWith("!")) {
					lcExcludeTypes.add(type.substring(1).toLowerCase(Locale.ROOT));
				} else {
					lcIncludeTypes.add(type.toLowerCase(Locale.ROOT));
				}
			}
		}

		return listCloudDatumStreams(restClient, objectMapper, filter).stream().filter(c -> {
			if (!(lcExcludeTypes.isEmpty() && lcIncludeTypes.isEmpty())) {
				final String serviceIdent = c.serviceIdentifier();
				if (excludeServiceIdents.contains(serviceIdent)) {
					return false;
				} else if (includeServiceIdents.contains(serviceIdent)) {
					return true;
				}
				for (String lcExclude : lcExcludeTypes) {
					if (serviceIdent.contains(lcExclude)) {
						excludeServiceIdents.add(serviceIdent);
						return false;
					}
				}
				for (String lcInclude : lcIncludeTypes) {
					if (serviceIdent.contains(lcInclude)) {
						includeServiceIdents.add(serviceIdent);
						return true;
					}
				}
				final String lcServiceName = datumStreamServiceLocalizedName(c.serviceIdentifier())
						.toLowerCase(Locale.ROOT);
				for (String lcExclude : lcExcludeTypes) {
					if (lcServiceName.contains(lcExclude)) {
						excludeServiceIdents.add(serviceIdent);
						return false;
					}
				}
				for (String lcInclude : lcIncludeTypes) {
					if (lcServiceName.contains(lcInclude)) {
						includeServiceIdents.add(serviceIdent);
						return true;
					}
				}
				if (!lcIncludeTypes.isEmpty()) {
					excludeServiceIdents.add(serviceIdent);
					return false;
				}
				includeServiceIdents.add(serviceIdent);
			}
			return true;
		}).collect(toMap(CloudDatumStreamConfiguration::configId, identity(), (_, n) -> n, TreeMap::new));
	}

}
