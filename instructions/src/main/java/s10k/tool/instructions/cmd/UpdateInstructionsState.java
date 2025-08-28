/**
 * 
 */
package s10k.tool.instructions.cmd;

import static java.util.Arrays.asList;
import static s10k.tool.common.util.RestUtils.checkSuccess;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.solarnetwork.domain.InstructionStatus.InstructionState;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import s10k.tool.common.cmd.BaseSubCmd;

/**
 * Update the state of a set of instructions matching a search filter.
 */
@Component
@Command(name = "update-state")
public class UpdateInstructionsState extends BaseSubCmd<InstructionsCmd> implements Callable<Integer> {

	@Option(names = { "-instruction",
			"--instruction-id" }, description = "an instruction ID to validate", split = "\\s*,\\s*", splitSynopsisLabel = ",", paramLabel = "instructionId")
	Long[] instructionIds;

	@Option(names = { "-node",
			"--node-id" }, description = "a node ID to return instructions for", split = "\\s*,\\s*", splitSynopsisLabel = ",", paramLabel = "nodeId")
	Long[] nodeIds;

	@Option(names = { "-state",
			"--state" }, description = "an instruction state to match", split = "\\s*,\\s*", splitSynopsisLabel = ",", paramLabel = "state")
	InstructionState[] instructionStates;

	@Parameters(index = "0", description = "the desired state to set the matching instructions to")
	InstructionState desiredState;

	/**
	 * Constructor.
	 * 
	 * @param reqFactory   the HTTP request factory to use
	 * @param objectMapper the mapper to use
	 */
	public UpdateInstructionsState(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		if ((instructionIds == null || instructionIds.length < 1) && (nodeIds == null || nodeIds.length < 1)
				&& (instructionStates == null || instructionStates.length < 1)) {
			System.err.println("Must provide at least one query filter option.");
			return 1;
		}

		// @formatter:off
		final InstructionsFilter filter = new InstructionsFilter(
				instructionIds != null ? asList(instructionIds) : null,
				nodeIds != null ? asList(nodeIds) : null,
				instructionStates != null ? asList(instructionStates) : null,
				null,
				null);
		// @formatter:on

		final RestClient restClient = restClient();

		try {
			Collection<Long> updatedIds = updateInstructionState(restClient, objectMapper, filter, desiredState);
			if (updatedIds.isEmpty()) {
				System.out.println("No instructions matched your criteria.");
			} else {
				System.out.println(Ansi.AUTO.string("Updated @|bold %d|@ instructions to @|bold %s|@: %s".formatted(
						updatedIds.size(), desiredState, StringUtils.collectionToCommaDelimitedString(updatedIds))));
			}
		} catch (Exception e) {
			System.err.println("Error updating instructions state: %s".formatted(e.getMessage()));
		}
		return 1;
	}

	/**
	 * Execute an instruction given a request map.
	 * 
	 * @param restClient   the REST client
	 * @param objectMapper the object mapper
	 * @param filter       the query filter
	 * @param desiredState the state to update instructions to
	 * @return the IDs of all updated instructions
	 * @throws IllegalStateException if the instruction listing is not available
	 */
	private static Collection<Long> updateInstructionState(RestClient restClient, ObjectMapper objectMapper,
			InstructionsFilter filter, InstructionState desiredState) {
		assert filter != null;

		MultiValueMap<String, Object> postBody = filter.toRequestMap();
		postBody.set("state", desiredState);

		// @formatter:off
		JsonNode response = restClient.post()
			.uri("/solaruser/api/v1/sec/instr/updateState")
			.contentType(MediaType.APPLICATION_FORM_URLENCODED)
			.body(postBody)
			.accept(MediaType.APPLICATION_JSON)
			.retrieve()
			.body(JsonNode.class)
			;		
		// @formatter:on

		checkSuccess(response);

		Set<Long> result = new LinkedHashSet<>(response.path("data").size());
		for (JsonNode node : response.path("data")) {
			if (node.isNumber()) {
				result.add(node.longValue());
			}
		}
		return result;
	}

}
