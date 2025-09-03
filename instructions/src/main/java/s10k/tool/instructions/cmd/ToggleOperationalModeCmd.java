package s10k.tool.instructions.cmd;

import static org.springframework.util.StringUtils.arrayToCommaDelimitedString;
import static s10k.tool.common.util.DateUtils.zonedDate;
import static s10k.tool.instructions.util.InstructionsUtils.executeInstruction;
import static s10k.tool.instructions.util.InstructionsUtils.populateExecutionDateParameter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.Callable;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.solarnetwork.domain.BasicInstruction;
import net.solarnetwork.domain.InstructionStatus;
import net.solarnetwork.domain.InstructionStatus.InstructionState;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.ScopeType;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.util.LocalDateTimeConverter;
import s10k.tool.instructions.domain.InstructionRequest;

/**
 * Enable and disable operational modes.
 */
@Component
@Command(name = "toggle-op-mode", sortSynopsis = false)
public class ToggleOperationalModeCmd extends BaseSubCmd<InstructionsCmd> implements Callable<Integer> {

	// @formatter:off
	@Option(names = { "-node", "--node-id" },
			description = "a node ID to toggle the operational mode on",
			required = true,
			scope = ScopeType.INHERIT)
	Long nodeId;

	@Option(names = { "-d", "--disable" },
			description = "disable the operational mode, instead of enabling")
	boolean disable;
	
	@Option(names = { "--mode-expiration" },
			description = "an expiration date when enabling a mode to automatically disable the mode",
			converter = LocalDateTimeConverter.class)
	LocalDateTime modeExpiration;

	@Option(names = { "-x", "--expiration" },
			description = "a date to automatically transition the instruction to Declined if not completed",
			converter = LocalDateTimeConverter.class)
	LocalDateTime expiration;

	@Option(names = { "-X", "--exec-at" },
			description = "a date to defer instruction execution until",
			converter = LocalDateTimeConverter.class)
	LocalDateTime executionDate;
	
	@Option(names = { "-tz", "--time-zone" },
			description = "a time zone to interpret all date options as, instead of the local time zone")
	ZoneId zone;

	@Parameters(arity =  "1..*",
			description = "the desired operational mode to toggle",
			paramLabel = "mode")
	String[] operationalModes;
	// @formatter:on

	/**
	 * Constructor.
	 * 
	 * @param reqFactory   the HTTP request factory to use
	 * @param objectMapper the mapper to use
	 */
	public ToggleOperationalModeCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		if (disable && modeExpiration != null) {
			System.err.println("Mode expiration can only be specified when enabling an operational mode.");
			return 1;
		}
		final BasicInstruction instr = new BasicInstruction(null,
				(disable ? "DisableOperationalModes" : "EnableOperationalModes"), null, null);
		for (String opMode : operationalModes) {
			instr.addParameter("OpMode", opMode);
		}
		if (!disable && modeExpiration != null) {
			instr.addParameter("Expiration",
					String.valueOf(zonedDate(modeExpiration, zone).toInstant().toEpochMilli()));
		}
		populateExecutionDateParameter(instr, zonedDate(executionDate, zone));

		final InstructionRequest req = new InstructionRequest(nodeId, instr, zonedDate(expiration, zone));

		final RestClient restClient = restClient();
		try {
			InstructionStatus status = executeInstruction(restClient, objectMapper, req);
			if (status.getInstructionState() == InstructionState.Completed) {
				var buf = new StringBuilder();
				// @formatter:off
				buf.append("%s operational modes [%s]".formatted(
						(!disable ? (executionDate != null && !zonedDate(executionDate, zone).isBefore(
										ZonedDateTime.now(ZoneId.systemDefault()))
									? "Will enable"
									: "Enabled")
								: "Disabled"),
						arrayToCommaDelimitedString(operationalModes)));
				// @formatter:on
				if (executionDate != null) {
					buf.append(" at ").append(executionDate);
				}
				buf.append(".");
				System.out.println(buf.toString());
				return 0;
			} else if (status.getInstructionState() == InstructionState.Declined) {
				System.out.println(Ansi.AUTO.string("@|red %s [%s] operational modes was refused.|@".formatted(
						(!disable ? "Enabling" : "Disabling"), arrayToCommaDelimitedString(operationalModes))));
				return 2;
			}
			System.out.print(Ansi.AUTO
					.string("""
							@|yellow %s operational modes [%s] is %s.|@ You can manually check its status using instruction ID @|bold %d|@.
							"""
							.formatted((!disable ? "Enabling" : "Disabling"),
									arrayToCommaDelimitedString(operationalModes), status.getInstructionState(),
									status.getInstructionId())));
			return 3;
		} catch (Exception e) {
			System.err.println(Ansi.AUTO.string("Error setting operating state: %s".formatted(e.getMessage())));
		}
		return 1;
	}

}
