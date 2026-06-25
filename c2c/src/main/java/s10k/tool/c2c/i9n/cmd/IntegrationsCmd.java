package s10k.tool.c2c.i9n.cmd;

import picocli.CommandLine.Command;
import s10k.tool.c2c.cmd.CloudIntegrationsCmd;
import s10k.tool.common.cmd.BaseSubCmd;

/**
 * Cloud Integration configurations commands.
 */
// @formatter:off
@Command(name = "integrations", aliases = "i9n", subcommands = {
		ListIntegrationsCmd.class
})
// @formatter:on
public class IntegrationsCmd extends BaseSubCmd<CloudIntegrationsCmd> {

}
