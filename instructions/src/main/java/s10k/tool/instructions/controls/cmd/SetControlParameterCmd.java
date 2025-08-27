package s10k.tool.instructions.controls.cmd;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.solarnetwork.domain.InstructionStatus;
import net.solarnetwork.domain.InstructionStatus.InstructionState;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import s10k.tool.common.cmd.BaseSubCmd;

/**
 * Set a control value.
 */
@Component
@Command(name = "set")
public class SetControlParameterCmd extends BaseSubCmd<ControlsCmd> implements Callable<Integer> {

	@Option(names = { "-node", "--node-id" }, description = "a node ID to set the control value on", required = true)
	Long nodeId;

	@Option(names = { "-control", "--control-id" }, description = "the control ID to validate", required = true)
	String controlId;

	@Parameters(index = "0", description = "the value to set the control to")
	String value;

	/**
	 * Constructor.
	 * 
	 * @param reqFactory   the HTTP request factory to use
	 * @param objectMapper the mapper to use
	 */
	public SetControlParameterCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		final RestClient restClient = restClient();
		try {
			InstructionStatus status = executeSetControlParameter(restClient);
			if (status.getInstructionState() == InstructionState.Completed) {
				System.out.println("Control [%s] set to [%s]".formatted(controlId, value));
			} else if (status.getInstructionState() == InstructionState.Declined) {
				System.out.println(
						Ansi.AUTO.string("@|red Setting [%s] to [%s] was refused.|@".formatted(controlId, value)));
			} else {
				System.out.print(Ansi.AUTO
						.string("""
								@|yellow Setting [%s] to [%s] is %s.|@ You can manually check its status using instruction ID @|bold %d|@.
								"""
								.formatted(controlId, value, status.getInstructionState(), status.getInstructionId())));
			}
		} catch (Exception e) {
			System.err.println(Ansi.AUTO.string("Error setting control parameter: %s".formatted(e.getMessage())));
		}
		return 1;
	}

	private InstructionStatus executeSetControlParameter(RestClient restClient) {
		var postBody = new LinkedHashMap<String, Object>(2);
		postBody.put("nodeId", nodeId);
		postBody.put("params", Map.of(controlId, value));
		// @formatter:off
		JsonNode result = restClient.post()
			.uri("/solaruser/api/v1/sec/instr/exec/SetControlParameter")
			.contentType(MediaType.APPLICATION_JSON)
			.body(postBody)
			.accept(MediaType.APPLICATION_JSON)
			.retrieve()
			.body(JsonNode.class)
			.path("data")
			;		
		// @formatter:on
		if (result.isObject()) {
			InstructionStatus status;
			try {
				status = objectMapper.treeToValue(result, InstructionStatus.class);
			} catch (JsonProcessingException | IllegalArgumentException e) {
				throw new RuntimeException("Error parsing instruction result: " + e.getMessage(), e);
			}
			return status;
		}
		throw new IllegalStateException("No instruction result available.");
	}

}
