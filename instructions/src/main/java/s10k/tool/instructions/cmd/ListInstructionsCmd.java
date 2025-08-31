package s10k.tool.instructions.cmd;

import static java.util.Arrays.asList;
import static s10k.tool.common.util.TableUtils.basicTable;
import static s10k.tool.instructions.util.InstructionsUtils.listInstructions;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
import s10k.tool.common.util.LocalDateTimeConverter;

/**
 * List instruction status.
 */
@Component
@Command(name = "list")
public class ListInstructionsCmd extends BaseSubCmd<InstructionsCmd> implements Callable<Integer> {

	// @formatter:off
	@Option(names = { "-id", "--instruction-id" },
		description = "an instruction ID to validate",
		split = "\\s*,\\s*",
		splitSynopsisLabel = ",",
		paramLabel = "instructionId")
	Long[] instructionIds;

	@Option(names = { "-node", "--node-id" },
		description = "a node ID to return instructions for",
		split = "\\s*,\\s*",
		splitSynopsisLabel = ",",
		paramLabel = "nodeId")
	Long[] nodeIds;

	@Option(names = { "-state", "--state" },
		description = "an instruction state to match",
		split = "\\s*,\\s*",
		splitSynopsisLabel = ",",
		paramLabel = "state")
	InstructionState[] instructionStates;

	@Option(names = { "-min", "--min-date" },
		description = "a minimum instruction creation date to match",
		converter = LocalDateTimeConverter.class)
	LocalDateTime minDate;

	@Option(names = { "-max", "--max-date" },
		description = "a maximum instruction creation date (exclusive) to match",
		converter = LocalDateTimeConverter.class)
	LocalDateTime maxDate;

	@Option(names = { "-tz", "--time-zone" },
		description = "a time zone to interpret the min and max dates as, instead of the local time zone")
	ZoneId zone;
	// @formatter:on

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
				&& (instructionStates == null || instructionStates.length < 1) && minDate == null && maxDate == null) {
			System.err.println("Must provide at least one query filter option.");
			return 1;
		}

		final RestClient restClient = restClient();
		// @formatter:off
		final InstructionsFilter filter = new InstructionsFilter(
				instructionIds != null ? asList(instructionIds) : null,
				nodeIds != null ? asList(nodeIds) : null,
				instructionStates != null ? asList(instructionStates) : null,
				zonedDate(minDate, zone),
				zonedDate(maxDate, zone));
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
				tableData.put("date", status.getStatusDate().atZone(zone != null ? zone : ZoneId.systemDefault()));

				Map<String, String> parameters = instr.getParameterMap();
				if (parameters != null && !parameters.isEmpty()) {
					tableData.put("params", basicTable(parameters, "Parameter", "Value", false));
				}

				if (status.getResultParameters() != null && !status.getResultParameters().isEmpty()) {
					tableData.put("result", basicTable(status.getResultParameters(), "Result", "Value", false));
				}
				System.out.print(basicTable(tableData, "Property", "Value", false));
			}
			return 0;
		} catch (Exception e) {
			System.err.println("Error listing instructions: %s".formatted(e.getMessage()));
		}
		return 1;
	}

	/**
	 * Get a zoned date time from a local date time and optional time zone.
	 * 
	 * @param date the local date
	 * @param zone the time zone, or {@code null} to use the system default
	 * @return the zoned date time, or {@code null} if {@code date} is {@code null}
	 */
	public static ZonedDateTime zonedDate(LocalDateTime date, ZoneId zone) {
		if (date == null) {
			return null;
		}
		return date.atZone(zone != null ? zone : ZoneId.systemDefault());
	}

}
