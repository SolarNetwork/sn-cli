package s10k.tool.common.cmd;

import static net.solarnetwork.util.ObjectUtils.nonnull;

import java.time.Instant;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import picocli.CommandLine.Command;
import picocli.CommandLine.ParentCommand;
import s10k.tool.common.domain.ProfileInfo;
import s10k.tool.common.domain.ProfileProvider;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.util.RestUtils;

/**
 * Base class for a sub-command implementation.
 */
@Command
public abstract class BaseSubCmd<P extends ProfileProvider> implements ProfileProvider {

	@ParentCommand
	protected @Nullable P parentCmd;

	/** The client HTTP request factory. */
	protected final @Nullable ClientHttpRequestFactory reqFactory;

	/** The ObjectMapper. */
	protected final @Nullable ObjectMapper objectMapper;

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
	public BaseSubCmd(@Nullable ClientHttpRequestFactory reqFactory, @Nullable ObjectMapper objectMapper) {
		super();
		this.reqFactory = reqFactory;
		this.objectMapper = objectMapper;
	}

	@Override
	public @Nullable ProfileInfo profile() {
		return (parentCmd != null ? parentCmd.profile() : null);
	}

	/**
	 * Get the root tool command.
	 * 
	 * @return the root tool command, or {@code null}
	 */
	protected @Nullable ToolCmd toolCmd() {
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
	 * Set the parent command.
	 * 
	 * @param parentCmd the parent command to set
	 */
	public void setParentCmd(P parentCmd) {
		this.parentCmd = parentCmd;
	}

	/**
	 * Get the verbosity level.
	 * 
	 * @return the verbosity level, with {@code 0} for "not verbose"
	 */
	protected int verbosity() {
		ToolCmd cmd = toolCmd();
		return (cmd != null ? cmd.verbosity() : 0);
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
	 * Get the "dry run" mode.
	 * 
	 * @return {@code true} if a dry run is desired
	 */
	protected boolean isDryRun() {
		final ToolCmd cmd = toolCmd();
		return (cmd != null ? cmd.isDryRun() : false);
	}

	/**
	 * Get the {@link ObjectMapper}.
	 *
	 * <p>
	 * This method is designed to be used when the {@code ObjectMapper} is known not
	 * to be {@code null}, to avoid nullness warnings.
	 * </p>
	 *
	 * @return the mapper (presumed non-null)
	 */
	@SuppressWarnings("NullAway")
	public final ObjectMapper objectMapper() {
		return objectMapper;
	}

	/**
	 * Get a REST client.
	 * 
	 * @return the client
	 * @throws IllegalArgumentException if profile credentials, request factory, or
	 *                                  object mapper are not available
	 */
	protected RestClient restClient() {
		final ProfileInfo profile = profileWithCredentials();
		final Instant now = Instant.now();
		final RestClient restClient = RestUtils.createSolarNetworkRestClient(
				nonnull(reqFactory, "ClientHttpRequestFactory"), profile,
				profile.tokenCredentials().credentialsProvider(now), nonnull(objectMapper, "ObjectMapper"),
				RestUtils.DEFAULT_SOLARNETWORK_BASE_URL, isTraceHttp());
		return restClient;
	}

	/**
	 * Get the display mode.
	 * 
	 * @param mode the optional mode to override the profile default
	 * @return the mode
	 */
	protected ResultDisplayMode displayMode(@Nullable ResultDisplayMode mode) {
		if (mode != null) {
			return mode;
		}
		ProfileInfo profile = profile();
		return (profile != null ? profile.displayMode() : ResultDisplayMode.PRETTY);
	}

}
