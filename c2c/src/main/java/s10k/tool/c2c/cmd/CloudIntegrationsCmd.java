package s10k.tool.c2c.cmd;

import picocli.CommandLine.Command;
import s10k.tool.c2c.i9n.cmd.IntegrationsCmd;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.cmd.ToolCmd;

/**
 * Cloud Integrations commands.
 */
// @formatter:off
@Command(name = "cloud-integrations", aliases = "c2c", subcommands = {
		IntegrationsCmd.class
})
// @formatter:on
public class CloudIntegrationsCmd extends BaseSubCmd<ToolCmd> {

}
