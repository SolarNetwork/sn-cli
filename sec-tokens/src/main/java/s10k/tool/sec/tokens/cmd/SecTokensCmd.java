package s10k.tool.sec.tokens.cmd;

import picocli.CommandLine.Command;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.cmd.ToolCmd;

/**
 * Security token commands.
 */
//@formatter:off
@Command(name = "sec-tokens", aliases = "tokens", subcommands = {
		CreateSecTokenCmd.class,
		DeleteSecTokenCmd.class,
		ListSecTokensCmd.class,
		UpdateSecTokenCmd.class,
})
//@formatter:on
public class SecTokensCmd extends BaseSubCmd<ToolCmd> {

}
