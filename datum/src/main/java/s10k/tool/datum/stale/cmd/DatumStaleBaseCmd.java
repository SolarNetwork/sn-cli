package s10k.tool.datum.stale.cmd;

import picocli.CommandLine.Command;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.datum.cmd.DatumCmd;

/**
 * Datum stale-aggregates commands.
 */
// @formatter:off
@Command(name = "stale-aggregates", aliases = { "stale-agg", "stale" }, subcommands = {
		ListStaleAggregatesCmd.class,
		MarkAggregatesStaleCmd.class,
})
// @formatter:on
public class DatumStaleBaseCmd extends BaseSubCmd<DatumCmd> {

}
