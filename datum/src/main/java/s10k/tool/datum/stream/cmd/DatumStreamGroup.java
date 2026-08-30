package s10k.tool.datum.stream.cmd;

import picocli.CommandLine.Command;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.datum.cmd.DatumGroup;

/**
 * Datum stream commands.
 */
// @formatter:off
@Command(name = "stream", subcommands = { 
		ListDatumStreamMetadataCmd.class,
		ListDatumStreamMetadataIdsCmd.class,
		RenameDatumStreamMetadataCmd.class,
		ViewDatumStreamMetadataCmd.class
})
// @formatter:on
public class DatumStreamGroup extends BaseSubCmd<DatumGroup> {

}
