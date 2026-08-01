package s10k.tool.datum.cmd;

import picocli.CommandLine.Command;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.cmd.ToolCmd;
import s10k.tool.datum.del.cmd.DatumDeleteBaseCmd;
import s10k.tool.datum.imp.cmd.DatumImportsCmd;
import s10k.tool.datum.stale.cmd.DatumStaleBaseCmd;
import s10k.tool.datum.stream.cmd.DatumStreamCmd;

/**
 * Datum commands.
 */
// @formatter:off
@Command(name = "datum", subcommands = {
		DatumDeleteBaseCmd.class,
		ListDatumCmd.class,
		DatumImportsCmd.class,
		DatumRangeCmd.class,
		DatumStaleBaseCmd.class,
		DatumStreamCmd.class
})
// @formatter:on
public class DatumCmd extends BaseSubCmd<ToolCmd> {

}
