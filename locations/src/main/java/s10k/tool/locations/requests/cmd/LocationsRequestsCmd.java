package s10k.tool.locations.requests.cmd;

import picocli.CommandLine.Command;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.locations.cmd.LocationsCmd;

/**
 * Location requests commands.
 */
@Command(name = "requests", aliases = { "reqs" }, subcommands = {
		//@formatter:off
		ListLocationRequestsCmd.class,
		//@formatter:on
})
public class LocationsRequestsCmd extends BaseSubCmd<LocationsCmd> {

}
