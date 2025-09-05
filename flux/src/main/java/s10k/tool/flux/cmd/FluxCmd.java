package s10k.tool.flux.cmd;

import picocli.CommandLine.Command;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.cmd.ToolCmd;

/**
 * Datum commands.
 */
// @formatter:off
@Command(name = "flux", subcommands = {
		StreamFluxCmd.class
})
// @formatter:on
public class FluxCmd extends BaseSubCmd<ToolCmd> {

}
