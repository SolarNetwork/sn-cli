package s10k.tool.instructions.controls.cmd;

import static s10k.tool.common.util.DateUtils.zonedDate;
import static s10k.tool.instructions.util.InstructionsUtils.executeInstruction;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.solarnetwork.domain.BasicInstruction;
import net.solarnetwork.domain.InstructionStatus;
import net.solarnetwork.domain.InstructionStatus.InstructionState;
import net.solarnetwork.util.StringUtils;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Option;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.instructions.domain.InstructionRequest;

/**
 * Activate a Control Conductor service.
 */
@Component
@Command(name = "orchestrate", sortSynopsis = false, showDefaultValues = true)
public class OrchestrateControlsCmd extends BaseSubCmd<ControlsCmd> implements Callable<Integer> {

	// @formatter:off
	@Option(names = { "-param", "--parameter" },
			description = "an extra instruction parameter to include, in the form name:value",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "parameter")
	String parameters[];
	// @formatter:on

	/**
	 * Constructor.
	 * 
	 * @param reqFactory   the HTTP request factory to use
	 * @param objectMapper the mapper to use
	 */
	public OrchestrateControlsCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		final RestClient restClient = restClient();

		final LocalDateTime executionDate = parentCmd.executionDate;
		if (executionDate == null) {
			System.err.println("The --exec-at option is required.");
			return 1;
		}

		final BasicInstruction instr = new BasicInstruction(null, "OrchestrateControls", null, null);
		instr.addParameter("service", parentCmd.controlId);

		final String dateParam = zonedDate(parentCmd.executionDate, parentCmd.zone).toInstant().toString();
		instr.addParameter("date", dateParam);

		if (parameters != null) {
			for (String parameter : parameters) {
				Map<String, String> paramMap = StringUtils.delimitedStringToMap(parameter, ",", ":");
				if (paramMap != null && !paramMap.isEmpty()) {
					for (Entry<String, String> e : paramMap.entrySet()) {
						instr.addParameter(e.getKey(), e.getValue());
					}
				}
			}
		}

		final InstructionRequest req = new InstructionRequest(parentCmd.nodeId, instr,
				zonedDate(parentCmd.expiration, parentCmd.zone));

		try {
			InstructionStatus status = executeInstruction(restClient, objectMapper, req);
			if (status.getInstructionState() == InstructionState.Completed) {
				System.out.println(Ansi.AUTO.string("Control [%s] received orchestrate instruction @|bold %d|@."
						.formatted(parentCmd.controlId, status.getInstructionId())));
				return 0;
			} else if (status.getInstructionState() == InstructionState.Declined) {
				System.out.println(Ansi.AUTO.string(
						"@|red Sending [%s] orchestrate instruction was refused.|@".formatted(parentCmd.controlId)));
				return 2;
			}
			System.out.print(Ansi.AUTO
					.string("""
							@|yellow Sending [%s] orchestrate instruction is %s.|@ You can manually check its status using instruction ID @|bold %d|@.
							"""
							.formatted(parentCmd.controlId, status.getInstructionState(), status.getInstructionId())));
			return 3;
		} catch (Exception e) {
			System.err.println(Ansi.AUTO.string("Error setting operating state: %s".formatted(e.getMessage())));
		}
		return 1;
	}

}
