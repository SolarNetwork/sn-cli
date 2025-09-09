package s10k.tool.instructions.settings.cmd;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ScopeType;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.instructions.cmd.InstructionsCmd;

/**
 * Settings commands.
 */
// @formatter:off
@Command(name = "settings", subcommands = {
		UpdateSettingsCmd.class,
		ViewSettingsCmd.class,
})
// @formatter:on
public class SettingsCmd extends BaseSubCmd<InstructionsCmd> {

	// @formatter:off
	@Option(names = { "-node", "--node-id" },
			description = "a ID of the node to manage",
			required = true,
			scope = ScopeType.INHERIT)
	Long nodeId;
	// @formatter:on

}
