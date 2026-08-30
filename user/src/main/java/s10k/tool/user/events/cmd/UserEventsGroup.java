package s10k.tool.user.events.cmd;

import picocli.CommandLine.Command;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.user.cmd.UserGroup;

/**
 * User events commands.
 */
//@formatter:off
@Command(name = "events", aliases = "events", subcommands = {
		ListUserEventsCmd.class,
})
//@formatter:on
public class UserEventsGroup extends BaseSubCmd<UserGroup> {

}
