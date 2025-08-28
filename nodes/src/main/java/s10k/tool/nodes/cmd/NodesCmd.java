package s10k.tool.nodes.cmd;

import picocli.CommandLine.Command;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.cmd.ToolCmd;
import s10k.tool.nodes.meta.cmd.NodeMetadataCmd;

/**
 * Node commands.
 */
@Command(name = "nodes", subcommands = { NodeMetadataCmd.class })
public class NodesCmd extends BaseSubCmd<ToolCmd> {

}
