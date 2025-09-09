package s10k.tool.instructions.cmd;

import static s10k.tool.instructions.cmd.InstructionsCmd.PARAM_SERVICE_RESULT;
import static s10k.tool.instructions.cmd.InstructionsCmd.TOPIC_SYSTEM_CONFIGURATION;
import static s10k.tool.instructions.util.InstructionsUtils.executeInstruction;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;

import net.solarnetwork.domain.BasicInstruction;
import net.solarnetwork.domain.InstructionStatus;
import net.solarnetwork.domain.InstructionStatus.InstructionState;
import net.solarnetwork.util.StringUtils;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Option;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.util.TableUtils;
import s10k.tool.instructions.domain.InstructionRequest;

/**
 * List node controls.
 */
@Component
@Command(name = "list-controls", sortSynopsis = false)
public class ListControlsCmd extends BaseSubCmd<InstructionsCmd> implements Callable<Integer> {

	/** The service name for listing controls. */
	public static final String CONTROLS_SERVICE = "net.solarnetwork.node.controls";

	// @formatter:off
	@Option(names = { "-node", "--node-id" },
			description = "a node ID to list the controls for",
			required = true)
	Long nodeId;

	@Option(names = { "-filter", "--filter" },
		description = "a wildcard filter to restrict the results to")
	String filter;
	
	@Option(names = { "-mode", "--display-mode" },
			description = "how to display the data",
			defaultValue = "PRETTY")
	ResultDisplayMode displayMode = ResultDisplayMode.PRETTY;
	// @formatter:on

	/**
	 * Constructor.
	 * 
	 * @param reqFactory   the HTTP request factory to use
	 * @param objectMapper the mapper to use
	 */
	public ListControlsCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		final RestClient restClient = restClient();

		final BasicInstruction instr = new BasicInstruction(null, TOPIC_SYSTEM_CONFIGURATION, null, null);
		instr.addParameter(InstructionsCmd.PARAM_SERVICE, CONTROLS_SERVICE);

		if (filter != null && !filter.isBlank()) {
			instr.addParameter("filter", filter);
		}

		final InstructionRequest req = new InstructionRequest(nodeId, instr, null);

		try {
			InstructionStatus status = executeInstruction(restClient, objectMapper, req);
			Map<String, ?> resultParams = status.getResultParameters();
			if (status.getInstructionState() == InstructionState.Completed) {
				Object controlIdsVal = (resultParams != null ? resultParams.get(PARAM_SERVICE_RESULT) : null);
				List<String> controlIds = StringUtils
						.commaDelimitedStringToList(controlIdsVal != null ? controlIdsVal.toString() : null);
				// @formatter:off
				TableUtils.renderTableData(new Column[] {
						new Column().header("Control ID").dataAlign(HorizontalAlign.LEFT)
				}, controlIds, displayMode, objectMapper, TableUtils.TableDataJsonPrettyPrinter.INSTANCE, System.out);
				// @formatter:on
				return 0;
			} else if (status.getInstructionState() == InstructionState.Declined) {
				System.err.println(Ansi.AUTO.string("@|red Listing controls was refused.|@"));
				return 2;
			}
			System.err.print(Ansi.AUTO
					.string("""
							@|yellow Listing controls instruction is %s.|@ You can manually check its status using instruction ID @|bold %d|@.
							"""
							.formatted(status.getInstructionState(), status.getInstructionId())));
			return 3;
		} catch (Exception e) {
			System.err.println(Ansi.AUTO.string("Error listing controls: %s".formatted(e.getMessage())));
		}
		return 1;
	}

}
