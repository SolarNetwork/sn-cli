package s10k.tool.c2c.ds.cmd;

import picocli.CommandLine.Command;
import s10k.tool.c2c.cmd.CloudIntegrationsGroup;
import s10k.tool.c2c.ds.poll.cmd.PollTasksGroup;
import s10k.tool.c2c.ds.rake.cmd.RakeTasksGroup;
import s10k.tool.common.cmd.BaseSubCmd;

/**
 * Cloud Datum Stream configurations commands.
 */
// @formatter:off
@Command(name = "datum-streams", aliases = "ds", subcommands = {
		CreateDatumStreamCmd.class,
		DatumStreamsReportCmd.class,
		DeleteDatumStreamsCmd.class,
		ListDatumStreamDatumCmd.class,
		ListDatumStreamsCmd.class,
		ViewDatumStreamCmd.class,
		ViewDataValuesCmd.class,
		PollTasksGroup.class,
		RakeTasksGroup.class,
		UpdateDatumStreamCmd.class,
		UpdateDatumStreamServicePropertiesCmd.class,
})
// @formatter:on
public class DatumStreamsGroup extends BaseSubCmd<CloudIntegrationsGroup> {

}
