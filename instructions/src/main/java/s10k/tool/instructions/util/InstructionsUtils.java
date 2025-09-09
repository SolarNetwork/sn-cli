package s10k.tool.instructions.util;

import static java.nio.charset.StandardCharsets.UTF_8;
import static s10k.tool.common.util.RestUtils.checkSuccess;
import static s10k.tool.common.util.RestUtils.populateQueryParameters;
import static s10k.tool.instructions.cmd.InstructionsCmd.PARAM_SERVICE_RESULT;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.solarnetwork.domain.BasicInstruction;
import net.solarnetwork.domain.Instruction;
import net.solarnetwork.domain.InstructionStatus;
import s10k.tool.instructions.domain.InstructionRequest;
import s10k.tool.instructions.domain.InstructionsFilter;

/**
 * Helper methods for instructions.
 */
public final class InstructionsUtils {

	/** The default maximum time to wait for instruction results. */
	public static final Duration DEFAULT_INSTRUCTION_RESULT_MAX_WAIT = Duration.ofSeconds(30);

	/** The execution date parameter. */
	public static final String EXECUTION_DATE_PARAM = "executionDate";

	private InstructionsUtils() {
		// not available
	}

	/**
	 * A service info result item.
	 */
	@RegisterReflectionForBinding
	public static record ServiceInfo(String id, String title) {
		// nothing
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
	 * Execute an instruction given an instruction request.
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
		// @formatter:on

		checkSuccess(response);

		InstructionStatus status = parseInstructionStatus(objectMapper, response.path("data"));
		if (status != null) {
			return status;
		}
		throw new IllegalStateException("No instruction result available.");
	}

	/**
	 * Execute an instruction given an instruction request, with a default maximum
	 * wait.
	 * 
	 * @param restClient   the REST client
	 * @param objectMapper the object mapper
	 * @param request      the instruction to execute
	 * @return the instruction status
	 * @throws IllegalStateException if the instruction result is not available
	 */
	public static InstructionStatus executeInstruction(RestClient restClient, ObjectMapper objectMapper,
			InstructionRequest request) {
		return executeInstruction(restClient, objectMapper, request, null);
	}

	/**
	 * Execute an instruction.
	 * 
	 * @param restClient   the REST client
	 * @param objectMapper the object mapper
	 * @param request      the instruction to execute
	 * @param maxWait      the maximum amount of time to wait for the instruction
	 *                     result; if {@code null} then
	 *                     {@link #DEFAULT_INSTRUCTION_RESULT_MAX_WAIT} will be
	 *                     used; if {@code 0} then this method will return
	 *                     immediately without waiting
	 * @return the instruction status
	 * @throws IllegalStateException if the instruction result is not available
	 */
	public static InstructionStatus executeInstruction(RestClient restClient, ObjectMapper objectMapper,
			InstructionRequest request, Duration maxWait) {
		// force this to "add" instead of "exec" if instruction is deferred
		final Duration wait = (maxWait == null && request.hasExecutionDate() ? Duration.ZERO : maxWait);
		// @formatter:off
		JsonNode response = restClient.post()
			.uri(b -> {
				return b.path("/solaruser/api/v1/sec/instr/{fn}/{topic}")
						.queryParam("resultMaxWait", (wait != null 
							? wait
							: DEFAULT_INSTRUCTION_RESULT_MAX_WAIT).toMillis())
						.build(
							wait != null && !wait.isPositive() ? "add" : "exec",
							request.topic()
						);
			})
			.contentType(MediaType.APPLICATION_JSON)
			.body(request)
			.accept(MediaType.APPLICATION_JSON)
			.retrieve()
			.body(JsonNode.class)
			;
		// @formatter:on

		checkSuccess(response);

		InstructionStatus status = parseInstructionStatus(objectMapper, response.path("data"));
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
				populateQueryParameters(b, filter::toRequestMap);
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

	/**
	 * Add an "execution date" parameter to an instruction.
	 * 
	 * @param instr         the instruction
	 * @param executionDate the date to add (may be {@code null} to do nothing)
	 */
	public static void populateExecutionDateParameter(BasicInstruction instr, ZonedDateTime executionDate) {
		if (instr == null || executionDate == null) {
			return;
		}
		instr.addParameter(EXECUTION_DATE_PARAM, executionDate.toInstant().toString());
	}

	/**
	 * Parse a compressed service result.
	 * 
	 * @param <T>          the result type
	 * @param resultParams the instruction result parameter map with the
	 *                     {@code result} property
	 * @param objectMapper the object mapper to use
	 * @param clazz        the expected object type
	 * @return the list of results
	 * @throws IOException if any IO error occurs
	 */
	public static <T> List<T> parseCompressedResultList(Map<String, ?> resultParams, ObjectMapper objectMapper,
			Class<T[]> clazz) throws IOException {
		Object base64JsonResult = (resultParams != null ? resultParams.get(PARAM_SERVICE_RESULT) : null);
		if (base64JsonResult == null) {
			return List.of();
		}
		try (InputStream in = new GZIPInputStream(
				Base64.getDecoder().wrap(new ByteArrayInputStream(base64JsonResult.toString().getBytes(UTF_8))))) {
			T[] infos = objectMapper.readValue(in, clazz);
			return Arrays.asList(infos);
		}
	}

}
