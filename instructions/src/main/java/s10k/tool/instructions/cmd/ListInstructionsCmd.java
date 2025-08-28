package s10k.tool.instructions.cmd;

import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Arrays.asList;
import static s10k.tool.common.util.TableUtils.basicTable;
import static s10k.tool.instructions.util.InstructionsUtils.listInstructions;

import java.time.ZoneId;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.solarnetwork.domain.Instruction;
import net.solarnetwork.domain.InstructionStatus;
import net.solarnetwork.domain.InstructionStatus.InstructionState;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import s10k.tool.common.cmd.BaseSubCmd;

/**
 * List instruction status.
 */
@Component
@Command(name = "list")
public class ListInstructionsCmd extends BaseSubCmd<InstructionsCmd> implements Callable<Integer> {

	@Option(names = { "-instruction",
			"--instruction-id" }, description = "an instruction ID to validate", split = "\\s*,\\s*", splitSynopsisLabel = ",", paramLabel = "instructionId")
	Long[] instructionIds;

	@Option(names = { "-node",
			"--node-id" }, description = "a node ID to return instructions for", split = "\\s*,\\s*", splitSynopsisLabel = ",", paramLabel = "nodeId")
	Long[] nodeIds;

	@Option(names = { "-state",
			"--state" }, description = "an instruction state to match", split = "\\s*,\\s*", splitSynopsisLabel = ",", paramLabel = "state")
	InstructionState[] instructionStates;

	/**
	 * Constructor.
	 * 
	 * @param reqFactory   the HTTP request factory to use
	 * @param objectMapper the mapper to use
	 */
	public ListInstructionsCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		if ((instructionIds == null || instructionIds.length < 1) && (nodeIds == null || nodeIds.length < 1)
				&& (instructionStates == null || instructionStates.length < 1)) {
			System.err.println("Must provide at least one query filter option.");
			return 1;
		}

		final RestClient restClient = restClient();
		// @formatter:off
		final InstructionsFilter filter = new InstructionsFilter(
				instructionIds != null ? asList(instructionIds) : null,
				nodeIds != null ? asList(nodeIds) : null,
				instructionStates != null ? asList(instructionStates) : null,
				null,
				null);
		// @formatter:on
		try {
			Collection<Instruction> instrs = listInstructions(restClient, objectMapper, filter);
			if (instrs == null) {
				System.out.println("No instructions matched your criteria.");
				return 0;
			}
			boolean multi = false;
			for (Instruction instr : instrs) {
				if (multi) {
					System.out.println("");
				} else {
					multi = true;
				}
				InstructionStatus status = instr.getStatus();
				Map<String, Object> tableData = new LinkedHashMap<String, Object>(4);
				tableData.put("id", instr.getId());
				tableData.put("topic", instr.getTopic());
				tableData.put("state", instr.getInstructionState());
				tableData.put("date",
						status.getStatusDate().atZone(ZoneId.systemDefault()).toLocalDateTime().truncatedTo(SECONDS));

				Map<String, String> parameters = instr.getParameterMap();
				if (parameters != null && !parameters.isEmpty()) {
					tableData.put("params", basicTable(parameters, "Parameter", "Value", false));
				}

				if (status.getResultParameters() != null && !status.getResultParameters().isEmpty()) {
					tableData.put("result", basicTable(status.getResultParameters(), "Result", "Value", false));
				}
				// @formatter:off
				System.out.print(basicTable(tableData, "Property", "Value", false));
				// @formatter:on
			}
			return 0;
		} catch (Exception e) {
			System.err.println("Error listing instructions: %s".formatted(e.getMessage()));
		}
		return 1;
	}

}
