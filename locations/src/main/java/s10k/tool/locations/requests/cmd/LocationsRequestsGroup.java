package s10k.tool.locations.requests.cmd;

import picocli.CommandLine.Command;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.locations.cmd.LocationsGroup;

/**
 * Location requests commands.
 */
@Command(name = "requests", aliases = { "reqs" }, subcommands = {
		//@formatter:off
		CreateRequestCmd.class,
		DeleteRequestCmd.class,
		ListLocationRequestsCmd.class,
		ViewLocationRequestCmd.class,
		//@formatter:on
})
public class LocationsRequestsGroup extends BaseSubCmd<LocationsGroup> {

}
