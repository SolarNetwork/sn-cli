package s10k.tool.common.util;

import static net.solarnetwork.util.ObjectUtils.requireNonNullArgument;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Dynamically resolve service URLs for client requests.
 */
public class ServiceUrlResolver implements ClientHttpRequestInterceptor {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private final Map<String, URI> serviceUrls;

	/**
	 * Constructor.
	 * 
	 * @param serviceUrls the service URLs
	 */
	public ServiceUrlResolver(Map<String, ?> serviceUrls) {
		requireNonNullArgument(serviceUrls, "serviceUrls");
		final Map<String, URI> urls = new LinkedHashMap<>(serviceUrls.size());
		for (Entry<String, ?> e : serviceUrls.entrySet()) {
			Object val = e.getValue();
			if (val != null) {
				try {
					urls.put(e.getKey(), new URI(val.toString()));
				} catch (URISyntaxException ex) {
					log.warn("Malformed service URL for service [{}]: {}", e.getKey(), val);
				}
			}
		}
		this.serviceUrls = urls;
	}

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {
		if (!(request instanceof ClientHttpRequest clientRequest)) {
			return execution.execute(request, body);
		}
		final String reqPath = request.getURI().getPath();
		final String serviceKey;
		if (reqPath.startsWith("/solaruser")) {
			serviceKey = "solaruser";
		} else if (reqPath.startsWith("/solarquery")) {
			serviceKey = "solarquery";
		} else {
			serviceKey = null;
		}
		final URI configBase = (serviceKey != null ? serviceUrls.get(serviceKey) : null);
		if (configBase == null) {
			return execution.execute(request, body);
		}
		// @formatter:off
		UriComponentsBuilder b = UriComponentsBuilder.fromUri(request.getURI())
				.scheme(configBase.getScheme())
				.host(configBase.getHost())
				.port(configBase.getPort())
				;
		// @formatter:on
		if (configBase.getPath() != null && !configBase.getPath().isEmpty() && !configBase.getPath().equals("/")) {
			b.replacePath(configBase.getPath() + request.getURI().getPath());
		}
		return execution.execute(new ReplaceUriClientHttpRequest(b.build(true).toUri(), clientRequest), body);
	}

	private static class ReplaceUriClientHttpRequest extends HttpRequestWrapper implements ClientHttpRequest {

		private final URI uri;

		private ReplaceUriClientHttpRequest(URI uri, ClientHttpRequest request) {
			super(request);
			this.uri = uri;
		}

		@Override
		public URI getURI() {
			return this.uri;
		}

		@Override
		public OutputStream getBody() throws IOException {
			return getRequest().getBody();
		}

		@Override
		public ClientHttpResponse execute() throws IOException {
			return getRequest().execute();
		}

		@Override
		public ClientHttpRequest getRequest() {
			return (ClientHttpRequest) super.getRequest();
		}

	}

}
