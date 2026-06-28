package s10k.tool.c2c.ds.poll.cmd;

import picocli.CommandLine.Command;
import s10k.tool.c2c.ds.cmd.DatumStreamsCmd;
import s10k.tool.common.cmd.BaseSubCmd;

/**
 * Cloud Datum Stream poll task configurations commands.
 */
// @formatter:off
@Command(name = "poll-tasks", aliases = "polls", subcommands = {
		ListTasksCmd.class,
})
// @formatter:on
public class PollTasksCmd extends BaseSubCmd<DatumStreamsCmd> {

}
