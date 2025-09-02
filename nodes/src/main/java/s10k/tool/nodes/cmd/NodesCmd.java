package s10k.tool.nodes.cmd;

import picocli.CommandLine.Command;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.cmd.ToolCmd;
import s10k.tool.nodes.meta.cmd.NodeMetadataCmd;

/**
 * Node commands.
 */
// @formatter:off
@Command(name = "nodes", subcommands = {
		ListNodeIdsCmd.class,
		NodeMetadataCmd.class
})
// @formatter:on
public class NodesCmd extends BaseSubCmd<ToolCmd> {

}
