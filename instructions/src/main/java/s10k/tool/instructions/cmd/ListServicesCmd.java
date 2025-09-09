package s10k.tool.instructions.cmd;

import static s10k.tool.instructions.cmd.InstructionsCmd.TOPIC_SYSTEM_CONFIGURATION;
import static s10k.tool.instructions.util.InstructionsUtils.executeInstruction;
import static s10k.tool.instructions.util.InstructionsUtils.parseCompressedResultList;

import java.io.IOException;
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
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Option;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.util.TableUtils;
import s10k.tool.instructions.domain.InstructionRequest;
import s10k.tool.instructions.util.InstructionsUtils.ServiceInfo;

/**
 * List available services or component service instances.
 */
@Component
@Command(name = "list-services", sortSynopsis = false)
public class ListServicesCmd extends BaseSubCmd<InstructionsCmd> implements Callable<Integer> {

	// @formatter:off
	@Option(names = { "-node", "--node-id" },
			description = "a node ID to list the services for",
			required = true)
	Long nodeId;

	@Option(names = { "-c", "--component-id" },
			description = "the ID of the component to view the service instances for")
	String componentId;
	
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
	public ListServicesCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		final boolean listComponentInstance = (componentId != null && !componentId.isBlank());
		final RestClient restClient = restClient();

		final BasicInstruction instr = new BasicInstruction(null, TOPIC_SYSTEM_CONFIGURATION, null, null);
		instr.addParameter(InstructionsCmd.PARAM_SERVICE, InstructionsCmd.SYSTEM_CONFIGURATION_SETTINGS_SERVICE);
		instr.addParameter("compress", "true");
		if (listComponentInstance) {
			instr.addParameter("uid", componentId);
			instr.addParameter("id", "*");
		} else {
			instr.addParameter("uid", "*");
		}

		final InstructionRequest req = new InstructionRequest(nodeId, instr, null);

		try {
			InstructionStatus status = executeInstruction(restClient, objectMapper, req);
			Map<String, ?> resultParams = status.getResultParameters();
			if (status.getInstructionState() == InstructionState.Completed) {
				// @formatter:off
				List<ServiceInfo> services = (listComponentInstance
						? parseInstanceIds(resultParams)
						: parseCompressedResultList(resultParams, objectMapper, ServiceInfo[].class));
				List<?> data = (displayMode == ResultDisplayMode.JSON ? services : 
						services.stream().map(p -> new Object[] {
								p.id(),
								p.title(),
						}).toList()
					);
				TableUtils.renderTableData(new Column[] {
						new Column().header("ID").dataAlign(HorizontalAlign.LEFT),
						new Column().header( "Title").dataAlign(HorizontalAlign.LEFT),
				}, data, displayMode, objectMapper, System.out);
				// @formatter:on
				return 0;
			} else if (status.getInstructionState() == InstructionState.Declined) {
				System.err.println(Ansi.AUTO.string("@|red Listing services was refused.|@"));
				return 2;
			}
			System.err.print(Ansi.AUTO
					.string("""
							@|yellow Listing services instruction is %s.|@ You can manually check its status using instruction ID @|bold %d|@.
							"""
							.formatted(status.getInstructionState(), status.getInstructionId())));
			return 3;
		} catch (Exception e) {
			System.err.println(Ansi.AUTO.string("Error services components: %s".formatted(e.getMessage())));
		}
		return 1;
	}

	private List<ServiceInfo> parseInstanceIds(Map<String, ?> resultParams) throws IOException {
		List<String> instanceIds = parseCompressedResultList(resultParams, objectMapper, String[].class);
		return instanceIds.stream().map(s -> new ServiceInfo(componentId, s)).toList();
	}

}
