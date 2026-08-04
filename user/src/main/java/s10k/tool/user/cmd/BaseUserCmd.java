package s10k.tool.user.cmd;

import picocli.CommandLine.Command;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.cmd.ToolCmd;
import s10k.tool.user.events.cmd.BaseUserEventsCmd;

/**
 * User commands.
 */
//@formatter:off
@Command(name = "user", subcommands = {
		BaseUserEventsCmd.class,
})
//@formatter:on
public class BaseUserCmd extends BaseSubCmd<ToolCmd> {

}
