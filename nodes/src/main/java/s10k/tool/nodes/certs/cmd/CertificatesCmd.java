package s10k.tool.nodes.certs.cmd;

import picocli.CommandLine.Command;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.cmd.ToolCmd;

/**
 * Nodes certificates command.
 */
//@formatter:off
@Command(name = "certificates", aliases = "certs", subcommands = {
		CreateCmd.class,
		DownloadCmd.class,
		RenewCmd.class,
		ReportCmd.class,
})
//@formatter:on
public class CertificatesCmd extends BaseSubCmd<ToolCmd> {

}
