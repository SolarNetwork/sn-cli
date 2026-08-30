package s10k.tool.c2c.mapping;

import picocli.CommandLine.Command;
import s10k.tool.c2c.cmd.CloudIntegrationsGroup;
import s10k.tool.common.cmd.BaseSubCmd;

/**
 * Cloud Integration mapping commands.
 */
// @formatter:off
@Command(name = "mappings", aliases = "maps", subcommands = {
		CreateMappingCmd.class,
		DeleteMappingsCmd.class,
		ListMappingsCmd.class,
})
// @formatter:on
public class MappingsGroup extends BaseSubCmd<CloudIntegrationsGroup> {

}
