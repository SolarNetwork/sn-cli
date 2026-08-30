package s10k.tool.nodes.cmd;

import picocli.CommandLine.Command;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.cmd.ToolCmd;
import s10k.tool.nodes.certs.cmd.CertificatesGroup;
import s10k.tool.nodes.meta.cmd.NodeMetadataGroup;

/**
 * Node commands.
 */
// @formatter:off
@Command(name = "nodes", subcommands = {
		CertificatesGroup.class,
		ListNodeIdsCmd.class,
		ListNodesCmd.class,
		ListSourcesCmd.class,
		NodeMetadataGroup.class
})
// @formatter:on
public class NodesGroup extends BaseSubCmd<ToolCmd> {

}
