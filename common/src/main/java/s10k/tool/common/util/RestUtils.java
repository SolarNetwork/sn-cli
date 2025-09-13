package s10k.tool.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.function.Supplier;

import org.springframework.http.MediaType;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.cbor.MappingJackson2CborHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriBuilder;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;

import net.solarnetwork.web.jakarta.security.AuthorizationCredentialsProvider;
import net.solarnetwork.web.jakarta.support.AuthorizationV2RequestInterceptor;
import net.solarnetwork.web.jakarta.support.LoggingHttpRequestInterceptor;
import s10k.tool.common.domain.SnTokenCredentialsInfo;

/**
 * Helper utilities for REST operations.
 */
public final class RestUtils {

	/** The default base URL to the SolarNetwork API. */
	public static final String DEFAULT_SOLARNETWORK_BASE_URL = "https://data.solarnetwork.net";

	private RestUtils() {
		// not available
	}

	/**
	 * Set the ObjectMapper used by a {@link RestTemplate}.
	 * 
	 * @param template     the template to adjust
	 * @param objectMapper the object mapper to use
	 */
	public static void setObjectMapper(RestTemplate template, ObjectMapper objectMapper) {
		for (HttpMessageConverter<?> converter : template.getMessageConverters()) {
			if (converter instanceof MappingJackson2CborHttpMessageConverter c) {
				c.setObjectMapper(objectMapper.copyWith(new CBORFactory()));
			} else if (converter instanceof MappingJackson2HttpMessageConverter c) {
				c.setObjectMapper(objectMapper);
			}
		}
	}

	/**
	 * Create a new {@link RestClient} instance.
	 * 
	 * <p>
	 * The client will automatically add a SolarNetwork API authorization header to
	 * each request.
	 * </p>
	 * 
	 * @param reqFactory   the request factory
	 * @param credProvider the SolarNetwork API credentials provider
	 * @param objectMapper the object mapper
	 * @param baseUrl      the base URL
	 * @param traceHttp    {@code true} to enable HTTP trace logging
	 * @return the client
	 */
	public static RestClient createSolarNetworkRestClient(ClientHttpRequestFactory reqFactory,
			AuthorizationCredentialsProvider credProvider, ObjectMapper objectMapper, String baseUrl,
			boolean traceHttp) {
		if (traceHttp) {
			RestTemplate template = new RestTemplate(new BufferingClientHttpRequestFactory(reqFactory));
			template.setInterceptors(
					List.of(new AuthorizationV2RequestInterceptor(credProvider), new LoggingHttpRequestInterceptor()));
			RestUtils.setObjectMapper(template, objectMapper);
			return RestClient.builder(template).baseUrl(baseUrl).build();
		}
		RestTemplate template = new RestTemplate(reqFactory);
		template.setInterceptors(List.of(new AuthorizationV2RequestInterceptor(credProvider)));
		RestUtils.setObjectMapper(template, objectMapper);
		return RestClient.builder(template).baseUrl(baseUrl).build();
	}

	/**
	 * Validate the success response property.
	 * 
	 * @param response the response to validate
	 * @throws IllegalStateException if the response does not have a {@code true}
	 *                               success value
	 */
	public static void checkSuccess(JsonNode response) {
		if (!response.path("success").booleanValue()) {
			throw new IllegalStateException(
					"Non-success response returned: " + response.path("message").asText("Unknown reason."));
		}
	}

	/**
	 * Convert a CBOR stream into JSON.
	 * 
	 * @param objectMapper the object mapper to use
	 * @param in           the input stream
	 * @param out          the output stream
	 * @throws IOException if any IO error occurs
	 */
	public static void cborToJson(ObjectMapper objectMapper, InputStream in, OutputStream out) throws IOException {
		try (JsonParser p = objectMapper.copyWith(new CBORFactory()).createParser(in);
				JsonGenerator g = objectMapper.createGenerator(out)) {
			while (p.nextToken() != null) {
				g.copyCurrentEvent(p);
			}
		}
	}

	/**
	 * Helper to populate URI query parameters from a map provider.
	 * 
	 * @param uriBuilder  the builder
	 * @param mapProvider the map provider
	 */
	public static void populateQueryParameters(UriBuilder uriBuilder, Supplier<MultiValueMap<String, ?>> mapProvider) {
		MultiValueMap<String, ?> params = mapProvider.get();
		for (var e : params.entrySet()) {
			uriBuilder.queryParam(e.getKey(), e.getValue());
		}
	}

	/**
	 * Get the active credentials information.
	 * 
	 * <p>
	 * This can be used to discover the user ID associated with the active
	 * credentials.
	 * </p>
	 * 
	 * @param restClient the REST client
	 * @return the credentials information
	 */
	public static SnTokenCredentialsInfo credentialsInfo(RestClient restClient) {
		assert restClient != null;
		// @formatter:off
		JsonNode response = restClient.get()
			.uri("/solarquery/api/v1/sec/whoami")
			.accept(MediaType.APPLICATION_JSON)
			.retrieve()
			.body(JsonNode.class)
			;		
		// @formatter:on

		checkSuccess(response);

		JsonNode node = response.path("data");
		String token = node.path("token").textValue();
		String tokenType = node.path("tokenType").textValue();

		JsonNode userIdNode = node.path("userId");
		Long userId = (userIdNode.isNumber() ? userIdNode.longValue() : null);
		return new SnTokenCredentialsInfo(token, tokenType, userId);
	}

}
