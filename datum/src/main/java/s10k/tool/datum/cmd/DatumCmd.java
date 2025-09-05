package s10k.tool.datum.cmd;

import picocli.CommandLine.Command;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.cmd.ToolCmd;
import s10k.tool.datum.stream.cmd.DatumStreamCmd;

/**
 * Datum commands.
 */
// @formatter:off
@Command(name = "datum", subcommands = {
		ListDatumCmd.class,
		DatumStreamCmd.class
})
// @formatter:on
public class DatumCmd extends BaseSubCmd<ToolCmd> {

}
