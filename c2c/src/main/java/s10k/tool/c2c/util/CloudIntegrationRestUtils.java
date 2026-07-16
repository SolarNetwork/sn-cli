package s10k.tool.c2c.util;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static s10k.tool.c2c.util.CloudIntegrationsUtils.datumStreamServiceLocalizedName;
import static s10k.tool.common.util.RestUtils.checkSuccess;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.SequencedMap;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import s10k.tool.c2c.domain.CloudDatumStreamConfiguration;
import s10k.tool.c2c.domain.CloudIntegrationConfiguration;
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

	/**
	 * View cloud integration.
	 * 
	 * @param restClient    the REST client
	 * @param objectMapper  the object mapper
	 * @param integrationId the integration ID
	 * @return the result
	 */
	public static CloudIntegrationConfiguration viewCloudIntegration(RestClient restClient, ObjectMapper objectMapper,
			Long integrationId) {
		// @formatter:off
		JsonNode response = restClient.get()
			.uri(b -> b.path("/solaruser/api/v1/sec/user/c2c/integrations/{integrationId}")
				.build(integrationId)
			)
			.accept(MediaType.APPLICATION_JSON)
			.retrieve()
			.body(JsonNode.class)
			;		
		// @formatter:on

		checkSuccess(response);

		try {
			return objectMapper.treeToValue(response.path("data"), CloudIntegrationConfiguration.class);
		} catch (JsonProcessingException | IllegalArgumentException e) {
			throw new IllegalStateException("Error parsing cloud integration response: " + e.getMessage(), e);
		}
	}

	/**
	 * View cloud datum stream filters.
	 * 
	 * @param restClient           the REST client
	 * @param objectMapper         the object mapper
	 * @param datumStreamServiceId the datum stream service ID to look up the
	 *                             filters for
	 * @return a mapping of filter keys to associated names, never {@code null}
	 */
	public static SequencedMap<String, String> viewDatumStreamFilters(RestClient restClient, ObjectMapper objectMapper,
			String datumStreamServiceId) {
		// @formatter:off
		JsonNode response = restClient.get()
			.uri(b -> b.path("/solaruser/api/v1/sec/user/c2c/services/datum-streams/data-filters")
					.replaceQueryParam("identifier", datumStreamServiceId)
				.build()
			)
			.accept(MediaType.APPLICATION_JSON)
			.retrieve()
			.body(JsonNode.class)
			;		
		// @formatter:on

		checkSuccess(response);

		final JsonNode dataListNode = response.path("data");
		final SequencedMap<String, String> result = new LinkedHashMap<>(dataListNode.size());
		for (JsonNode dataNode : dataListNode) {
			final String id = dataNode.path("id").textValue();
			final String name = dataNode.path("localizedName").textValue();
			if (id != null && name != null) {
				result.put(id, name);
			}
		}

		return result;
	}

}
