package s10k.tool.common.cmd;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParseResult;
import s10k.tool.common.domain.ProfileInfo;
import s10k.tool.common.domain.ProfileProvider;
import s10k.tool.common.util.ProfileUtils;

/**
 * Top-level command.
 */
@Command(name = "s10k")
public class ToolCmd implements ProfileProvider {

	@Option(names = { "-v", "--verbose" }, description = "verbose output")
	boolean[] verbosity;

	@Option(names = { "-n", "--dry-run" }, description = "do not make any changes")
	boolean dryRun;

	@Option(names = { "--http-trace" }, description = "trace HTTP exchanges")
	boolean traceHttp;

	@Option(names = { "-h", "--help" }, usageHelp = true, description = "display this help message")
	boolean usageHelpRequested;

	@Option(names = { "-p", "--profile" }, description = "profile to use")
	String profileName;

	private ProfileInfo profile;

	/**
	 * Globally initialize
	 * 
	 * @param parseResult the parse result
	 * @return the exit code result
	 */
	public int globalInit(ParseResult parseResult) {
		if (profileName != null && !profileName.isBlank()) {
			profile = ProfileUtils.profile(profileName);
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
