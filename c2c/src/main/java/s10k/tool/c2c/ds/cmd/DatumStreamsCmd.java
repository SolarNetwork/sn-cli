package s10k.tool.c2c.ds.cmd;

import picocli.CommandLine.Command;
import s10k.tool.c2c.cmd.CloudIntegrationsCmd;
import s10k.tool.c2c.ds.poll.cmd.PollTasksCmd;
import s10k.tool.c2c.ds.rake.cmd.RakeTasksCmd;
import s10k.tool.common.cmd.BaseSubCmd;

/**
 * Cloud Datum Stream configurations commands.
 */
// @formatter:off
@Command(name = "datum-streams", aliases = "ds", subcommands = {
		DatumStreamsReportCmd.class,
		ListDatumStreamDatumCmd.class,
		ListDatumStreamsCmd.class,
		ViewDatumStreamCmd.class,
		ViewDataValuesCmd.class,
		PollTasksCmd.class,
		RakeTasksCmd.class,
		UpdateDatumStreamCmd.class,
})
// @formatter:on
public class DatumStreamsCmd extends BaseSubCmd<CloudIntegrationsCmd> {

}
