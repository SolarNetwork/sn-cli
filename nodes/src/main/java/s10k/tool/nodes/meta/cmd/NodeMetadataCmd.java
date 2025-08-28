package s10k.tool.nodes.meta.cmd;

import picocli.CommandLine.Command;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.nodes.cmd.NodesCmd;

/**
 * Node commands.
 */
// @formatter:off
@Command(name = "meta", subcommands = {
		DeleteNodeMetadataCmd.class,
		ListNodeMetadataCmd.class,
		SaveNodeMetadataCmd.class
})
// @formatter:on
public class NodeMetadataCmd extends BaseSubCmd<NodesCmd> {

}
