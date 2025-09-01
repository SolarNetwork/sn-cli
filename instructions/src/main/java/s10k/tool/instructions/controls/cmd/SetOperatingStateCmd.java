package s10k.tool.instructions.controls.cmd;

import static s10k.tool.instructions.util.InstructionsUtils.executeInstruction;
import static s10k.tool.instructions.util.InstructionsUtils.simpleInstructionRequest;

import java.util.Map;
import java.util.concurrent.Callable;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.solarnetwork.domain.DeviceOperatingState;
import net.solarnetwork.domain.InstructionStatus;
import net.solarnetwork.domain.InstructionStatus.InstructionState;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Parameters;
import s10k.tool.common.cmd.BaseSubCmd;

/**
 * Set an operating state.
 */
@Component
@Command(name = "set-operating-state", aliases = "set-op-state")
public class SetOperatingStateCmd extends BaseSubCmd<ControlsCmd> implements Callable<Integer> {

	@Parameters(index = "0", description = "the operating state to set", paramLabel = "desiredState")
	DeviceOperatingState value;

	/**
	 * Constructor.
	 * 
	 * @param reqFactory   the HTTP request factory to use
	 * @param objectMapper the mapper to use
	 */
	public SetOperatingStateCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		final RestClient restClient = restClient();
		final Map<String, ?> request = simpleInstructionRequest(parentCmd.nodeId, parentCmd.controlId, value.name());
		try {
			InstructionStatus status = executeInstruction(restClient, objectMapper, "SetOperatingState", request);
			if (status.getInstructionState() == InstructionState.Completed) {
				System.out.println("Control [%s] operating state set to [%s]".formatted(parentCmd.controlId, value));
			} else if (status.getInstructionState() == InstructionState.Declined) {
				System.out.println(Ansi.AUTO.string("@|red Setting [%s] operating state to [%s] was refused.|@"
						.formatted(parentCmd.controlId, value)));
			} else {
				System.out.print(Ansi.AUTO
						.string("""
								@|yellow Setting [%s] operating state to [%s] is %s.|@ You can manually check its status using instruction ID @|bold %d|@.
								"""
								.formatted(parentCmd.controlId, value, status.getInstructionState(),
										status.getInstructionId())));
			}
			return 0;
		} catch (Exception e) {
			System.err.println(Ansi.AUTO.string("Error setting operating state: %s".formatted(e.getMessage())));
		}
		return 1;
	}

}
