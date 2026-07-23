package s10k.tool.datum.imp.cmd;

import picocli.CommandLine.Command;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.datum.cmd.DatumCmd;

/**
 * Datum import commands.
 */
// @formatter:off
@Command(name = "imports", subcommands = {
		ListImportJobsCmd.class,
})
// @formatter:on
public class DatumImportCmd extends BaseSubCmd<DatumCmd> {

}
