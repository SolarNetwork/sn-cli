package s10k.tool.datum.imp.cmd;

import picocli.CommandLine.Command;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.datum.cmd.DatumCmd;

/**
 * Datum import commands.
 */
// @formatter:off
@Command(name = "imports", aliases = { "import", "imp" }, subcommands = {
		ConfirmStagedImportCmd.class,
		ImportDatumCmd.class,
		ListImportJobsCmd.class,
		PreviewStagedImportCmd.class,
		RetractImportCmd.class,
		UpdateImportJobCmd.class,
		ViewImportJobCmd.class,
})
// @formatter:on
public class DatumImportsCmd extends BaseSubCmd<DatumCmd> {

}
