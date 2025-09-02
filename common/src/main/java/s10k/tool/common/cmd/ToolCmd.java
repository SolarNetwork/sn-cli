package s10k.tool.common.cmd;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParseResult;
import s10k.tool.common.domain.ProfileInfo;
import s10k.tool.common.domain.ProfileProvider;
import s10k.tool.common.domain.SnTokenCredentials;
import s10k.tool.common.util.ProfileUtils;

/**
 * Top-level command.
 */
@Command(name = "s10k")
public class ToolCmd implements ProfileProvider {

	/** An environment variable name for a SolarNetwork token ID. */
	public static final String SN_TOKEN_ID_ENV = "SN_TOKEN_ID";

	/** An environment variable name for a SolarNetwork token secret. */
	public static final String SN_TOKEN_SECRET_ENV = "SN_TOKEN_SECRET";

	@Option(names = { "-v", "--verbose" }, description = "verbose output")
	boolean[] verbosity;

	@Option(names = { "-n", "--dry-run" }, description = "do not make any changes")
	boolean dryRun;

	@Option(names = { "--http-trace" }, description = "trace HTTP exchanges")
	boolean traceHttp;

	@Option(names = { "-h", "--help" }, usageHelp = true, description = "display this help message")
	boolean usageHelpRequested;

	@Option(names = { "-P", "--profile" }, description = "profile to use")
	String profileName;

	@Option(names = { "-u", "--token" }, description = "the SolarNetwork API token")
	String tokenId;

	@Option(names = { "-p", "--secret" }, description = "the SolarNetwork API token secret", interactive = true)
	char[] tokenSecret;

	private ProfileInfo profile;

	/**
	 * Globally initialize.
	 * 
	 * <p>
	 * This will populate the {@code profile}, either via the profile settings for
	 * {@code profileName}, the default profile if no {@code profileName} provided,
	 * the {@code tokenId} or {@code tokenSecret} options, or the
	 * {@link #SN_TOKEN_ID_ENV} and {@link #SN_TOKEN_SECRET_ENV} environment
	 * variables.
	 * </p>
	 * 
	 * @param parseResult the parse result
	 * @return the exit code result
	 */
	public int globalInit(ParseResult parseResult) {
		profile = ProfileUtils.profile(profileName);
		if (profileName == null || profile == null) {
			// use command options if available
			if (tokenId != null && !tokenId.isEmpty() && tokenSecret != null && tokenSecret.length > 0) {
				profile = new ProfileInfo("", new SnTokenCredentials(tokenId, tokenSecret));
			} else {
				// use environment variables
				String tokenId = System.getenv(SN_TOKEN_ID_ENV);
				String tokenSecret = System.getenv(SN_TOKEN_SECRET_ENV);
				if (tokenId != null && !tokenId.isEmpty() && tokenSecret != null && !tokenSecret.isEmpty()) {
					profile = new ProfileInfo("", new SnTokenCredentials(tokenId, tokenSecret.toCharArray()));
				}
			}
		}
		return new CommandLine.RunLast().execute(parseResult);
	}

	/**
	 * Get the verbosity level as an integer.
	 * 
	 * @return the verbosity level, starting at {@code 0} for "not verbose"
	 */
	public int verbosity() {
		return (verbosity != null ? verbosity.length : 0);
	}

	@Override
	public ProfileInfo profile() {
		return profile;
	}

	/**
	 * Get the verbosity level.
	 * 
	 * @return the verbosity (length of array)
	 */
	public boolean[] getVerbosity() {
		return verbosity;
	}

	/**
	 * Get the "dry run" mode.
	 * 
	 * @return {@code true} for "dry run" mode, where no changes are actually made
	 */
	public boolean isDryRun() {
		return dryRun;
	}

	/**
	 * Get the "HTTP trace" mode.
	 * 
	 * @return {@code true} to support HTTP trace logs
	 */
	public boolean isTraceHttp() {
		return traceHttp;
	}

	/**
	 * Get the "show help" flag.
	 * 
	 * @return {@code true} to display help
	 */
	public boolean isUsageHelpRequested() {
		return usageHelpRequested;
	}

}
