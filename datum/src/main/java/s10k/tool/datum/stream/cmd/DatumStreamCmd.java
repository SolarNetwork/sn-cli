/**
 * 
 */
package s10k.tool.datum.stream.cmd;

import picocli.CommandLine.Command;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.datum.cmd.DatumCmd;

/**
 * Datum stream commands.
 */
@Command(name = "stream", subcommands = { ListNodeDatumStreamMetadataCmd.class, ViewDatumStreamMetadataCmd.class })
public class DatumStreamCmd extends BaseSubCmd<DatumCmd> {

}
