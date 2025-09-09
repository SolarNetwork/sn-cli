package s10k.tool.instructions.cmd;

import static java.nio.charset.StandardCharsets.UTF_8;
import static s10k.tool.instructions.cmd.InstructionsCmd.PARAM_SERVICE_RESULT;
import static s10k.tool.instructions.cmd.InstructionsCmd.TOPIC_SYSTEM_CONFIGURATION;
import static s10k.tool.instructions.util.InstructionsUtils.executeInstruction;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.zip.GZIPInputStream;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
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

/**
 * List installed packages.
 */
@Component
@Command(name = "list-packages", sortSynopsis = false)
public class ListPackagesCmd extends BaseSubCmd<InstructionsCmd> implements Callable<Integer> {

	/** The service name for listing packages. */
	public static final String PACKAGES_SERVICE = "net.solarnetwork.node.packages";

	// @formatter:off
	@Option(names = { "-node", "--node-id" },
			description = "a node ID to set the control value on",
			required = true)
	Long nodeId;

	@Option(names = { "-filter", "--filter" },
			description = "a regular expression filter to restrict the results to",
			defaultValue = "^(sn-|solarnode)")
	String filter = "^(sn-|solarnode)";
	
	@Option(names = { "-s", "--status" },
			description = "the type of packages to list",
			defaultValue = "Installed")
	PackageStatus packageStatus = PackageStatus.Installed;

	@Option(names = { "-mode", "--display-mode" },
			description = "how to display the data",
			defaultValue = "PRETTY")
	ResultDisplayMode displayMode = ResultDisplayMode.PRETTY;
	// @formatter:on

	/**
	 * Package status.
	 */
	public static enum PackageStatus {
		/** Only installed packages. */
		Installed,

		/** Only available (not installed) packages. */
		Available,

		/** Installed and available packages. */
		All,
	}

	/**
	 * A package status result item.
	 */
	@RegisterReflectionForBinding
	public static record PackageInfo(String name, String version, boolean installed) {

	}

	/**
	 * Constructor.
	 * 
	 * @param reqFactory   the HTTP request factory to use
	 * @param objectMapper the mapper to use
	 */
	public ListPackagesCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		final RestClient restClient = restClient();

		final BasicInstruction instr = new BasicInstruction(null, TOPIC_SYSTEM_CONFIGURATION, null, null);
		instr.addParameter(InstructionsCmd.PARAM_SERVICE, PACKAGES_SERVICE);
		instr.addParameter("compress", "true");

		if (filter != null && !filter.isBlank()) {
			instr.addParameter("filter", filter);
		}
		if (packageStatus != null) {
			instr.addParameter("status", packageStatus.name());
		}

		final InstructionRequest req = new InstructionRequest(nodeId, instr, null);

		try {
			InstructionStatus status = executeInstruction(restClient, objectMapper, req);
			Map<String, ?> resultParams = status.getResultParameters();
			if (status.getInstructionState() == InstructionState.Completed) {
				Object base64JsonResult = (resultParams != null ? resultParams.get(PARAM_SERVICE_RESULT) : null);
				List<PackageInfo> packages = parsePackages(base64JsonResult);
				// @formatter:off
				List<?> data = (displayMode == ResultDisplayMode.JSON ? packages : 
						packages.stream().map(p -> new Object[] {
								p.name(),
								p.version,
								p.installed
						}).toList()
					);
				TableUtils.renderTableData(new Column[] {
						new Column().header("Name").dataAlign(HorizontalAlign.LEFT),
						new Column().header("Version").dataAlign(HorizontalAlign.RIGHT),
						new Column().header("Installed").dataAlign(HorizontalAlign.RIGHT)
				}, data, displayMode, objectMapper, System.out);
				// @formatter:on
				return 0;
			} else if (status.getInstructionState() == InstructionState.Declined) {
				System.err.println(Ansi.AUTO.string("@|red Listing packages was refused.|@"));
				return 2;
			}
			System.out.print(Ansi.AUTO
					.string("""
							@|yellow Listing packages instruction is %s.|@ You can manually check its status using instruction ID @|bold %d|@.
							"""
							.formatted(status.getInstructionState(), status.getInstructionId())));
			return 3;
		} catch (Exception e) {
			System.err.println(Ansi.AUTO.string("Error listing packages: %s".formatted(e.getMessage())));
		}
		return 1;
	}

	private List<PackageInfo> parsePackages(Object base64JsonResult) throws IOException {
		if (base64JsonResult == null) {
			return List.of();
		}
		try (InputStream in = new GZIPInputStream(
				Base64.getDecoder().wrap(new ByteArrayInputStream(base64JsonResult.toString().getBytes(UTF_8))))) {
			PackageInfo[] infos = objectMapper.readValue(in, PackageInfo[].class);
			return Arrays.asList(infos);
		}
	}

}
