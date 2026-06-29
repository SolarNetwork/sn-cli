package s10k.tool.c2c.ds.rake.cmd;

import picocli.CommandLine.Command;
import s10k.tool.c2c.ds.cmd.DatumStreamsCmd;
import s10k.tool.common.cmd.BaseSubCmd;

/**
 * Cloud Datum Stream rake task configurations commands.
 */
// @formatter:off
@Command(name = "rake-tasks", aliases = "rakes", subcommands = {
		CreateTasksCmd.class,
		ListTasksCmd.class,
})
// @formatter:on
public class RakeTasksCmd extends BaseSubCmd<DatumStreamsCmd> {

}
