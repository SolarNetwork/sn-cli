package s10k.tool.c2c.domain;

import picocli.CommandLine.Option;

/**
 * Grouping of enabled/disabled mode flags.
 */
public class EnabledOrDisabled {

	// @formatter:off
	@Option(names = {"-e", "--enabled"},
			description = "match only enabled entities")
	public boolean enabled;
	
	@Option(names = {"-d", "--disabled"},
			description = "match only disabled entities")
	public boolean disabled;
	// @formatter:on

}
