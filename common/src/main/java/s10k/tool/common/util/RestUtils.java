package s10k.tool.common.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.cbor.MappingJackson2CborHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;

import net.solarnetwork.web.jakarta.security.AuthorizationCredentialsProvider;
import net.solarnetwork.web.jakarta.support.AuthorizationV2RequestInterceptor;
import net.solarnetwork.web.jakarta.support.LoggingHttpRequestInterceptor;

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

}
