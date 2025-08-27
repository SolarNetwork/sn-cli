package s10k.tool.common.cmd;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;
import s10k.tool.common.domain.ProfileInfo;
import s10k.tool.common.domain.ProfileProvider;
import s10k.tool.common.util.RestUtils;

/**
 * Base class for a sub-command implementation.
 */
@Command
public abstract class BaseSubCmd<P extends ProfileProvider> implements ProfileProvider {

	@ParentCommand
	private P parentCmd;

	/** The client HTTP request factory. */
	protected final ClientHttpRequestFactory reqFactory;

	/** The ObjectMapper. */
	protected final ObjectMapper objectMapper;

	/** A class-level logger. */
	protected final Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * Construct without services.
	 */
	public BaseSubCmd() {
		this.reqFactory = null;
		this.objectMapper = null;
	}

	/**
	 * Constructor.
	 * 
	 * @param reqFactory   the HTTP request factory to use
	 * @param objectMapper the mapper to use
	 */
	public BaseSubCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super();
		this.reqFactory = reqFactory;
		this.objectMapper = objectMapper;
	}

	@Override
	public ProfileInfo profile() {
		return (parentCmd != null ? parentCmd.profile() : null);
	}

	/**
	 * Get the root tool command.
	 * 
	 * @return the root tool command, or {@code null}
	 */
	protected ToolCmd toolCmd() {
		Object cmd = parentCmd;
		while (true) {
			if (cmd == null) {
				break;
			} else if (cmd instanceof ToolCmd c) {
				return c;
			} else if (cmd instanceof BaseSubCmd<?> c) {
				cmd = c.parentCmd;
			}
		}
		return null;
	}

	/**
	 * Get the "trace HTTP" mode.
	 * 
	 * @return {@code true} if HTTP trace should be enabled
	 */
	protected boolean isTraceHttp() {
		final ToolCmd cmd = toolCmd();
		return (cmd != null ? cmd.isTraceHttp() : false);
	}

	/**
	 * Get a REST client.
	 * 
	 * @return the client
	 * @throws IllegalArgumentException if profile credentials are not available
	 */
	protected RestClient restClient() {
		final ProfileInfo profile = profileWithCredentials();
		final Instant now = Instant.now();
		final RestClient restClient = RestUtils.createSolarNetworkRestClient(reqFactory,
				profile.tokenCredentials().credentialsProvider(now), objectMapper,
				RestUtils.DEFAULT_SOLARNETWORK_BASE_URL, isTraceHttp());
		return restClient;
	}

}
