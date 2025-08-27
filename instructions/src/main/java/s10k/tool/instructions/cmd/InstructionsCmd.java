package s10k.tool.instructions.cmd;

import picocli.CommandLine.Command;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.cmd.ToolCmd;
import s10k.tool.instructions.controls.cmd.ControlsCmd;

/**
 * Instruction commands.
 */
@Command(name = "instructions", subcommands = { ControlsCmd.class, ListInstructionsCmd.class })
public class InstructionsCmd extends BaseSubCmd<ToolCmd> {

}
