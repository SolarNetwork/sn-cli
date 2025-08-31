package s10k.tool.instructions.controls.cmd;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ScopeType;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.instructions.cmd.InstructionsCmd;

/**
 * Control commands.
 */
@Command(name = "controls", subcommands = { SetControlParameterCmd.class, SetOperatingState.class })
public class ControlsCmd extends BaseSubCmd<InstructionsCmd> {

	// @formatter:off
	@Option(names = { "-node", "--node-id" },
			description = "a node ID to set the control value on",
			required = true,
			scope = ScopeType.INHERIT)
	Long nodeId;

	@Option(names = { "-control", "--control-id" },
			description = "the control ID to validate",
			required = true,
			scope = ScopeType.INHERIT)
	String controlId;	
	// @formatter:on

}
