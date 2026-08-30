package s10k.tool.datum.del.cmd;

import picocli.CommandLine.Command;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.datum.cmd.DatumGroup;

/**
 * Datum delete commands.
 */
// @formatter:off
@Command(name = "delete", aliases = { "del" }, subcommands = {
		DeleteDatumCmd.class,
		DeleteDatumIdsCmd.class,
		ViewDeleteJobCmd.class,
})
// @formatter:on
public class DatumDeleteGroup extends BaseSubCmd<DatumGroup> {

}
