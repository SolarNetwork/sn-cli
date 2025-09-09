package s10k.tool.instructions.controls.cmd;

import java.time.LocalDateTime;
import java.time.ZoneId;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ScopeType;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.util.LocalDateTimeConverter;
import s10k.tool.instructions.cmd.InstructionsCmd;

/**
 * Control commands.
 */
// @formatter:off
@Command(name = "controls", subcommands = {
		SetControlParameterCmd.class,
		SetOperatingStateCmd.class,
		SignalCmd.class,
})
// @formatter:on
public class ControlsCmd extends BaseSubCmd<InstructionsCmd> {

	// @formatter:off
	@Option(names = { "-node", "--node-id" },
			description = "a node ID to set the control value on",
			required = true,
			scope = ScopeType.INHERIT)
	Long nodeId;

	@Option(names = { "-control", "--control-id" },
			description = "the control ID to validate",
			required = true,
			scope = ScopeType.INHERIT)
	String controlId;
	
	@Option(names = { "-x", "--expiration" },
			description = "a date to automatically transition the instruction to Declined if not completed",
			converter = LocalDateTimeConverter.class,
			scope = ScopeType.INHERIT)
	LocalDateTime expiration;

	@Option(names = { "-X", "--exec-at" },
			description = "a date to defer instruction execution until",
			converter = LocalDateTimeConverter.class,
			scope = ScopeType.INHERIT)
	LocalDateTime executionDate;

	@Option(names = { "-tz", "--time-zone" },
			description = "a time zone to interpret all date options as, instead of the local time zone",
			scope = ScopeType.INHERIT)
	ZoneId zone;
	// @formatter:on

}
