package s10k.tool.locations.cmd;

import picocli.CommandLine.Command;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.cmd.ToolCmd;

/**
 * Location commands.
 */
// @formatter:off
@Command(name = "locations", aliases = { "locs" }, subcommands = {
		ListLocationsCmd.class,
})
// @formatter:on
public class LocationsCmd extends BaseSubCmd<ToolCmd> {

}
