package s10k.tool.c2c.cmd;

import picocli.CommandLine.Command;
import s10k.tool.c2c.ds.cmd.DatumStreamsGroup;
import s10k.tool.c2c.i9n.cmd.IntegrationsGroup;
import s10k.tool.c2c.mapping.MappingsGroup;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.cmd.ToolCmd;

/**
 * Cloud Integrations commands.
 */
// @formatter:off
@Command(name = "cloud-integrations", aliases = "c2c", subcommands = {
		DatumStreamsGroup.class,
		IntegrationsGroup.class,
		MappingsGroup.class,
})
// @formatter:on
public class CloudIntegrationsGroup extends BaseSubCmd<ToolCmd> {

}
