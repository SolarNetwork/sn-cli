package s10k.tool.nodes.meta.cmd;

import picocli.CommandLine.Command;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.nodes.cmd.NodesCmd;

/**
 * Node commands.
 */
@Command(name = "meta", subcommands = { ListNodeMetadataCmd.class })
public class NodeMetadataCmd extends BaseSubCmd<NodesCmd> {

}
