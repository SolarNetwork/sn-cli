package s10k.tool.instructions.settings.cmd;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.util.FileCopyUtils.copyToString;
import static s10k.tool.instructions.util.InstructionsUtils.executeInstruction;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.solarnetwork.domain.BasicInstruction;
import net.solarnetwork.domain.InstructionStatus;
import net.solarnetwork.domain.InstructionStatus.InstructionState;
import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.util.SystemUtils;
import s10k.tool.instructions.domain.InstructionRequest;
import s10k.tool.instructions.util.InstructionsUtils;

/**
 * Update settings.
 */
@Component
// @formatter:off
@Command(name = "update", sortSynopsis = false, descriptionHeading = "%n", optionListHeading = "%n", description = {
		"Update one or more settings on a node. The settings can be provided for a",
		"specific service or component instance using the @|bold --service-id|@ and",
		"@|bold --component-id|@ options, followed by key and value parameter pairs. For",
		"example, to update the schedule of a component to run every 5 seconds:%n",
		
		"s10k instructions settings update --component-id the.component --service-id \\",
		"  schedule '0/5 * * * * *'%n",
		
		"Alternatively the settings can be provided as CSV via standard input or via",
		"an @file.csv parameter. The CSV must contain a header row. If @|bold --service-id|@",
		"and optionally \"@|bold --component-id|@ options are provided the header must define",
		"@|bold Key|@ and @|bold Value|@ columns, otherwise the header must additionally define a @|bold Type|@",
		"column. The header names are case-insensitive.%n",
})
// @formatter:on
public class UpdateSettingsCmd extends BaseSubCmd<SettingsCmd> implements Callable<Integer> {

	// @formatter:off
	@Option(names = { "-s", "--service-id" },
			description = "the ID of the service to update the settings for")
	String serviceId;

	@Option(names = { "-c", "--component-id" },
			description = "the ID of the component to update the settings for")
	String componentId;
	
	@Parameters(index = "0",
			paramLabel = "<setting>",
			description = "the key and value setting pairs to update, or @file for CSV file to load",
			arity = "0..*")
	String[] settings;
	// @formatter:on

	/**
	 * Constructor.
	 * 
	 * @param reqFactory   the HTTP request factory to use
	 * @param objectMapper the mapper to use
	 */
	public UpdateSettingsCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	/**
	 * A setting record.
	 */
	public static record SettingInfo(String key, String type, String value) {
		// nothing
	}

	@Override
	public Integer call() throws Exception {
		try {
			String csv = null;
			if (settings == null || settings.length < 1) {
				// try stdin
				if (!SystemUtils.systemConsoleIsTerminal()) {
					csv = copyToString(new InputStreamReader(System.in, UTF_8));
				}
			} else if (settings[0].startsWith("@")) {
				Path metaPath = Paths.get(settings[0].substring(1));
				if (!Files.isReadable(metaPath)) {
					System.err.println("Settings file [%s] not available.".formatted(metaPath));
					return 1;
				}
				csv = copyToString(Files.newBufferedReader(metaPath, UTF_8));
			}

			final List<SettingInfo> settingInfos = (csv != null ? parseSettingsCsv(csv) : parseSettings(settings));
			if (settingInfos.isEmpty()) {
				System.err.println("No settings provided. Pass settings on the command line or standard input.");
				return 1;
			}

			final BasicInstruction instr = new BasicInstruction(null, "UpdateSetting", null, null);
			populateParameters(instr, settingInfos);

			final InstructionRequest req = new InstructionRequest(parentCmd.nodeId, instr, null);

			final RestClient restClient = restClient();
			InstructionStatus status = executeInstruction(restClient, objectMapper, req);
			if (status.getInstructionState() == InstructionState.Completed) {
				System.out.println("Settings applied.");
				return 0;
			} else if (status.getInstructionState() == InstructionState.Declined) {
				InstructionsUtils.printErrorMessageResult("@|red Updating settings was refused.|@", status, System.err);
				return 2;
			}
			System.err.print(Ansi.AUTO
					.string("""
							@|yellow Update settings instruction is %s.|@ You can manually check its status using instruction ID @|bold %d|@.
							"""
							.formatted(status.getInstructionState(), status.getInstructionId())));
			return 3;
		} catch (IllegalStateException e) {
			System.err.println(e.getMessage());
		} catch (Exception e) {
			System.err.println(Ansi.AUTO.string("Error updating settings: %s".formatted(e.getMessage())));
		}
		return 1;
	}

	/**
	 * Get a setting "static" key based on the {@code serviceId} and optional
	 * {@code componentId}.
	 * 
	 * @return the static setting key to use, or {@code null} if the key must come
	 *         from CSV data
	 */
	private String staticKey() {
		return (serviceId != null && !serviceId.isBlank()
				? (componentId != null && !componentId.isBlank() ? "%s.%s".formatted(componentId, serviceId)
						: serviceId)
				: null);
	}

	/**
	 * Parse setting updates in {@code type} {@code value} pairs.
	 * 
	 * @param settings the setting updates to parse; must be an even number of
	 *                 values
	 * @return the settings
	 * @throws IllegalStateException if at least {@code serviceId} is available
	 */
	private List<SettingInfo> parseSettings(String[] settings) {
		if (settings.length % 2 != 0) {
			throw new IllegalStateException(
					"Setting parameters must be provided in key and value pairs (an even number).");
		}
		final String staticKey = staticKey();
		if (staticKey == null) {
			throw new IllegalStateException("A service ID and optional component ID must be provided.");
		}
		final List<SettingInfo> result = new ArrayList<>();
		for (int i = 0; i < settings.length; i += 2) {
			result.add(new SettingInfo(staticKey, settings[i], settings[i + 1]));
		}
		return result;
	}

	/**
	 * Parse a CSV string of setting data.
	 * 
	 * <p>
	 * The CSV must provide a header row, the names in which will be treated
	 * case-insensitively. If a {@code serviceId} (and optional {@code componentId}
	 * are available then the CSV need only provide <b>Key</b> and <b>Value</b>
	 * columns and the <b>Key</b> column will be treated as the setting type.
	 * Otherwise the CSV must also provide a <b>Type</b> column.
	 * </p>
	 * 
	 * @param csv the CSV to parse
	 * @return the setting instances
	 * @throws IOException           if an IO error occurs
	 * @throws IllegalStateException if the appropriate columns cannot be determined
	 *                               from the header (first) row of CSV data
	 */
	private List<SettingInfo> parseSettingsCsv(String csv) throws IOException {
		final String staticKey = staticKey();
		final List<SettingInfo> result = new ArrayList<>();
		try (ICsvListReader csvReader = new CsvListReader(new StringReader(csv), CsvPreference.STANDARD_PREFERENCE)) {
			final String[] header = csvReader.getHeader(true);

			int keyCol = -1;
			int typeCol = -1;
			int valCol = -1;
			for (int i = 0; i < header.length; i++) {
				String colName = header[i];
				if (colName.equalsIgnoreCase("key")) {
					keyCol = i;
				} else if (colName.equalsIgnoreCase("type")) {
					typeCol = i;
				} else if (colName.equalsIgnoreCase("value")) {
					valCol = i;
				}
			}

			if (staticKey != null && typeCol < 0) {
				typeCol = keyCol;
			}

			if (keyCol < 0) {
				throw new IllegalStateException("No 'Key' column available in CSV header row.");
			} else if (typeCol < 0) {
				throw new IllegalStateException("No 'Type' column available in CSV header row.");
			} else if (valCol < 0) {
				throw new IllegalStateException("No 'Value' column available in CSV header row.");
			}

			for (List<String> row = csvReader.read(); row != null; row = csvReader.read()) {
				if (row.isEmpty() || (row.getFirst() != null && row.getFirst().startsWith("#"))) {
					// skip blank or commented rows
					continue;
				}
				String key = null;
				String type = null;
				String val = null;
				if (staticKey != null) {
					key = staticKey;
				} else if (keyCol < row.size()) {
					key = row.get(keyCol);
					if (key == null || key.isBlank()) {
						continue;
					}
				} else {
					continue;
				}
				if (typeCol < row.size()) {
					type = row.get(typeCol);
					if (type == null || type.isBlank()) {
						continue;
					}
				} else {
					continue;
				}
				if (valCol < row.size()) {
					val = row.get(valCol);
					if (val == null) {
						val = "";
					}
				} else {
					continue;
				}
				result.add(new SettingInfo(key, type, val));
			}
		}
		return result;
	}

	private void populateParameters(BasicInstruction instr, List<SettingInfo> settings) {
		for (SettingInfo setting : settings) {
			instr.addParameter("key", setting.key());
			instr.addParameter("type", setting.type());
			instr.addParameter("value", setting.value());
		}
	}

}
