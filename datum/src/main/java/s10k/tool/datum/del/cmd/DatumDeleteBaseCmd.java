package s10k.tool.datum.del.cmd;

import picocli.CommandLine.Command;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.datum.cmd.DatumCmd;

/**
 * Datum delete commands.
 */
// @formatter:off
@Command(name = "delete", aliases = { "del" }, subcommands = {
		DeleteDatumCmd.class,
		ViewDeleteJobCmd.class,
})
// @formatter:on
public class DatumDeleteBaseCmd extends BaseSubCmd<DatumCmd> {

}
