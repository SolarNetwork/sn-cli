package s10k.tool.instructions.util;

import static s10k.tool.common.util.RestUtils.checkSuccess;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.http.MediaType;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.solarnetwork.domain.Instruction;
import net.solarnetwork.domain.InstructionStatus;
import s10k.tool.instructions.cmd.InstructionsFilter;

/**
 * Helper methods for instructions.
 */
public final class InstructionsUtils {

	/** The default maximum time to wait for instruction results. */
	public static final Duration DEFAULT_INSTRUCTION_RESULT_MAX_WAIT = Duration.ofSeconds(30);

	private InstructionsUtils() {
		// not available
	}

	/**
	 * Parse an {@link InstructionStatus} value from a JSON node.
	 * 
	 * @param objectMapper the mapper to use
	 * @param json         the JSON node to parse
	 * @return the status, or {@code null} if {@code json} is {@code null} or not a
	 *         JSON object node
	 */
	public static InstructionStatus parseInstructionStatus(ObjectMapper objectMapper, JsonNode json) {
		if (json != null && json.isObject()) {
			InstructionStatus status;
			try {
				status = objectMapper.treeToValue(json, InstructionStatus.class);
			} catch (JsonProcessingException | IllegalArgumentException e) {
				throw new RuntimeException("Error parsing instruction result: " + e.getMessage(), e);
			}
			return status;
		}
		return null;
	}

	/**
	 * Construct a simple instruction request for a key-value pair.
	 * 
	 * @param nodeId the node ID
	 * @param key    the instruction parameter name
	 * @param val    the instruction parameter value
	 * @return the instruction request
	 */
	public static Map<String, ?> simpleInstructionRequest(Long nodeId, String key, String val) {
		return Map.of("nodeId", nodeId, "params", Map.of(key, val));
	}

	/**
	 * Execute an instruction given a request map with the default maximum wait
	 * time.
	 * 
	 * @param restClient      the REST client
	 * @param objectMapper    the object mapper
	 * @param instructionName the name of the instruction to execute
	 * @param request         the instruction request (must include a {@code nodeId}
	 *                        key) used
	 * @return the instruction status
	 * @throws IllegalStateException if the instruction result is not available
	 */
	public static InstructionStatus executeInstruction(RestClient restClient, ObjectMapper objectMapper,
			String instructionName, Map<String, ?> request) {
		return executeInstruction(restClient, objectMapper, instructionName, request, null);
	}

	/**
	 * Execute an instruction given a request map.
	 * 
	 * @param restClient      the REST client
	 * @param objectMapper    the object mapper
	 * @param instructionName the name of the instruction to execute
	 * @param request         the instruction request (must include a {@code nodeId}
	 *                        key)
	 * @param maxWait         the maximum amount of time to wait for the instruction
	 *                        result; if {@code null} then
	 *                        {@link #DEFAULT_INSTRUCTION_RESULT_MAX_WAIT} will be
	 *                        used
	 * @return the instruction status
	 * @throws IllegalStateException if the instruction result is not available
	 */
	public static InstructionStatus executeInstruction(RestClient restClient, ObjectMapper objectMapper,
			String instructionName, Map<String, ?> request, Duration maxWait) {
		// @formatter:off
		JsonNode response = restClient.post()
			.uri("/solaruser/api/v1/sec/instr/exec/%s?resultMaxWait=%d".formatted(
					instructionName, 
					(maxWait != null ? maxWait : DEFAULT_INSTRUCTION_RESULT_MAX_WAIT).toMillis()))
			.contentType(MediaType.APPLICATION_JSON)
			.body(request)
			.accept(MediaType.APPLICATION_JSON)
			.retrieve()
			.body(JsonNode.class)
			;
		
		checkSuccess(response);
		
		// @formatter:on
		InstructionStatus status = InstructionsUtils.parseInstructionStatus(objectMapper, response.path("data"));
		if (status != null) {
			return status;
		}
		throw new IllegalStateException("No instruction result available.");
	}

	/**
	 * Parse an {@link Instruction} value from a JSON node.
	 * 
	 * @param objectMapper the mapper to use
	 * @param json         the JSON node to parse
	 * @return the instruction, or {@code null} if {@code json} is {@code null} or
	 *         not a JSON object node
	 */
	public static Instruction parseInstruction(ObjectMapper objectMapper, JsonNode json) {
		if (json != null && json.isObject()) {
			Instruction instr;
			try {
				instr = objectMapper.treeToValue(json, Instruction.class);
			} catch (JsonProcessingException | IllegalArgumentException e) {
				throw new RuntimeException("Error parsing instruction result: " + e.getMessage(), e);
			}
			return instr;
		}
		return null;
	}

	/**
	 * List instructions matching a search filter.
	 * 
	 * @param restClient   the REST client
	 * @param objectMapper the object mapper
	 * @param filter       the query filter
	 * @return the instruction statuses
	 * @throws IllegalStateException if the instruction listing is not available
	 */
	public static Collection<Instruction> listInstructions(RestClient restClient, ObjectMapper objectMapper,
			InstructionsFilter filter) {
		assert filter != null;
		// @formatter:off
		JsonNode response = restClient.get()
			.uri(b -> {
				b.path("/solaruser/api/v1/sec/instr");
				MultiValueMap<String, Object> params = filter.toRequestMap();
				for ( Entry<String, List<Object>> e : params.entrySet() ) {
					b.queryParam(e.getKey(), e.getValue());
				}
				return b.build();
			})
			.accept(MediaType.APPLICATION_JSON)
			.retrieve()
			.body(JsonNode.class)
			;		
		// @formatter:on

		checkSuccess(response);

		List<Instruction> result = new ArrayList<>(response.path("data").size());
		for (JsonNode node : response.path("data")) {
			Instruction instr = InstructionsUtils.parseInstruction(objectMapper, node);
			if (instr != null) {
				result.add(instr);
			}
		}
		return result;
	}

}
