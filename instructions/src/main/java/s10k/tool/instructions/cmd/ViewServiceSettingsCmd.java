package s10k.tool.instructions.cmd;

import static s10k.tool.instructions.cmd.InstructionsCmd.TOPIC_SYSTEM_CONFIGURATION;
import static s10k.tool.instructions.util.InstructionsUtils.executeInstruction;
import static s10k.tool.instructions.util.InstructionsUtils.parseCompressedResultList;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
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

/**
 * List available (non-component) services.
 */
@Component
@Command(name = "view-service", sortSynopsis = false)
public class ViewServiceSettingsCmd extends BaseSubCmd<InstructionsCmd> implements Callable<Integer> {

	// @formatter:off
	@Option(names = { "-node", "--node-id" },
			description = "a node ID to list the services for",
			required = true)
	Long nodeId;
	
	@Option(names = { "-s", "--service-id" },
			description = "the ID of the service to view",
			required = true)
	String serviceId;

	@Option(names = { "-c", "--component-id" },
			description = "the ID of the component to view the service instance settings for")
	String componentId;
	
	@Option(names = { "-S", "--specification" },
			description = "get setting specifications instead of setting values")
	boolean specification;

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
	public ViewServiceSettingsCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	/**
	 * A service setting info result item.
	 */
	@RegisterReflectionForBinding
	public static record ServiceSettingInfo(String key, String value, @JsonProperty("default") boolean systemDefault) {
		// nothing
	}

	@Override
	public Integer call() throws Exception {
		final boolean viewComponentInstance = (componentId != null && !componentId.isBlank());
		final RestClient restClient = restClient();

		final BasicInstruction instr = new BasicInstruction(null, TOPIC_SYSTEM_CONFIGURATION, null, null);
		instr.addParameter(InstructionsCmd.PARAM_SERVICE, InstructionsCmd.SYSTEM_CONFIGURATION_SETTINGS_SERVICE);
		instr.addParameter("compress", "true");
		if (viewComponentInstance) {
			instr.addParameter("uid", componentId);
			instr.addParameter("id", serviceId);
		} else {
			instr.addParameter("uid", serviceId);
		}
		if (specification) {
			instr.addParameter("spec", "true");
		}

		final InstructionRequest req = new InstructionRequest(nodeId, instr, null);

		try {
			InstructionStatus status = executeInstruction(restClient, objectMapper, req);
			Map<String, ?> resultParams = status.getResultParameters();
			if (status.getInstructionState() == InstructionState.Completed) {
				if (specification) {
					List<JsonNode> specs = parseCompressedResultList(resultParams, objectMapper, JsonNode[].class);
					TableUtils.renderTableData(List.of(specs), ResultDisplayMode.JSON, objectMapper, System.out);
				} else {
					List<ServiceSettingInfo> services = parseCompressedResultList(resultParams, objectMapper,
							ServiceSettingInfo[].class);
					// @formatter:off
					List<?> data = (displayMode == ResultDisplayMode.JSON ? List.of(services) : 
							services.stream().map(p -> new Object[] {
									p.key(),
									p.value(),
									p.systemDefault()
							}).toList()
						);
					TableUtils.renderTableData(new Column[] {
							new Column().header("Key").dataAlign(HorizontalAlign.LEFT),
							new Column().header("Value").dataAlign(HorizontalAlign.LEFT),
							new Column().header("Default").dataAlign(HorizontalAlign.RIGHT),
					}, data, displayMode, objectMapper, System.out);
				}
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

}
