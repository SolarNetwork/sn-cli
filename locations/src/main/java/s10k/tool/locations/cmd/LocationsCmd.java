package s10k.tool.locations.cmd;

import picocli.CommandLine.Command;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.cmd.ToolCmd;
import s10k.tool.locations.requests.cmd.LocationsRequestsCmd;

/**
 * Location commands.
 */
@Command(name = "locations", aliases = { "locs" }, subcommands = {
		// @formatter:off
		ListLocationsCmd.class,
		LocationsRequestsCmd.class,
		// @formatter:on
})
public class LocationsCmd extends BaseSubCmd<ToolCmd> {

}
