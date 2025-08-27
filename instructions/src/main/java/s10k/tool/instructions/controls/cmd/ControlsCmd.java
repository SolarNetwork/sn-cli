package s10k.tool.instructions.controls.cmd;

import picocli.CommandLine.Command;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.instructions.cmd.InstructionsCmd;

/**
 * Control commands.
 */
@Command(name = "controls", subcommands = { SetControlParameterCmd.class })
public class ControlsCmd extends BaseSubCmd<InstructionsCmd> {

}
