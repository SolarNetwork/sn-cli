package s10k.tool.instructions.controls.cmd;

import static s10k.tool.common.util.DateUtils.zonedDate;
import static s10k.tool.instructions.util.InstructionsUtils.executeInstruction;
import static s10k.tool.instructions.util.InstructionsUtils.populateExecutionDateParameter;

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
import picocli.CommandLine.Parameters;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.instructions.domain.InstructionRequest;

/**
 * Signal a control.
 */
@Component
@Command(name = "signal", sortSynopsis = false)
public class SignalCmd extends BaseSubCmd<ControlsCmd> implements Callable<Integer> {

	@Parameters(index = "0", description = "the signal to send", paramLabel = "signal")
	String value;

	/**
	 * Constructor.
	 * 
	 * @param reqFactory   the HTTP request factory to use
	 * @param objectMapper the mapper to use
	 */
	public SignalCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		final RestClient restClient = restClient();

		final BasicInstruction instr = new BasicInstruction(null, "Signal", null, null);
		instr.addParameter(parentCmd.controlId, value);
		populateExecutionDateParameter(instr, zonedDate(parentCmd.executionDate, parentCmd.zone));

		final InstructionRequest req = new InstructionRequest(parentCmd.nodeId, instr,
				zonedDate(parentCmd.expiration, parentCmd.zone));

		try {
			InstructionStatus status = executeInstruction(restClient, objectMapper, req);
			if (status.getInstructionState() == InstructionState.Completed) {
				System.out.println("Control [%s] received [%s] signal.".formatted(parentCmd.controlId, value));
				return 0;
			} else if (status.getInstructionState() == InstructionState.Declined) {
				System.out.println(Ansi.AUTO
						.string("@|red Sending [%s] signal [%s] was refused.|@".formatted(parentCmd.controlId, value)));
				return 2;
			}
			System.out.print(Ansi.AUTO
					.string("""
							@|yellow Sending [%s] signal [%s] is %s.|@ You can manually check its status using instruction ID @|bold %d|@.
							"""
							.formatted(parentCmd.controlId, value, status.getInstructionState(),
									status.getInstructionId())));
			return 3;
		} catch (Exception e) {
			System.err.println(Ansi.AUTO.string("Error setting operating state: %s".formatted(e.getMessage())));
		}
		return 1;
	}

}
