package s10k.tool.datum.cmd;

import picocli.CommandLine.Command;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.cmd.ToolCmd;
import s10k.tool.datum.del.cmd.DatumDeleteGroup;
import s10k.tool.datum.imp.cmd.DatumImportsGroup;
import s10k.tool.datum.stale.cmd.DatumStaleGroup;
import s10k.tool.datum.stream.cmd.DatumStreamGroup;

/**
 * Datum commands.
 */
// @formatter:off
@Command(name = "datum", subcommands = {
		DatumDeleteGroup.class,
		ListDatumCmd.class,
		DatumImportsGroup.class,
		DatumRangeCmd.class,
		DatumStaleGroup.class,
		DatumStreamGroup.class
})
// @formatter:on
public class DatumGroup extends BaseSubCmd<ToolCmd> {

}
