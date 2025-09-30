package s10k.tool.nodes.certs.cmd;

import static com.github.freva.asciitable.HorizontalAlign.LEFT;
import static com.github.freva.asciitable.HorizontalAlign.RIGHT;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.util.FileCopyUtils.copyToString;
import static s10k.tool.nodes.cmd.ListNodeIdsCmd.listNodeIds;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.SequencedCollection;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.freva.asciitable.Column;

import me.tongfei.progressbar.ProgressBar;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.util.SystemUtils;
import s10k.tool.common.util.TableUtils;
import s10k.tool.nodes.domain.NodeCertificateInfo;

/**
 * Generate a report on account node certificates.
 */
@Component
@Command(name = "report", sortSynopsis = false, header = {
// @formatter:off
		"",
		"""
		Generate a report on node certificates, including their expiration dates.
		
		Passwords for each certificate must be provided as a table of node ID and
		password pairs. The data can be provided as CSV or JSON. For CSV it must include
		a header row and column names that match the --node-id-col and --password-col
		options, or exactly two columns one of which has node ID number values and the
		other assumed to be passwords.
		
		For JSON a top-level array is required, with either object elements with
		property names that match the --node-id-col and --password-col options, or a
		nested array with two elements in the form `[nodeId,\"password\"]`.
		""",
		// @formatter:on
})
public class ReportCmd extends BaseSubCmd<CertificatesCmd> implements Callable<Integer> {

	// @formatter:off
	@Option(names = { "-n", "--node-id-col" },
			description = "the name of the node ID column/property",
			defaultValue = "Node ID")
	String nodeIdColumnName = "Node ID";
	
	@Option(names = { "-p", "--password-col" },
			description = "the name of the certificate password column/property",
			defaultValue = "Certificate Password")
	String passwordColumnName = "Certificate Password";
	
	@Option(names = { "-N", "--node-id-regex" },
			description = "a regular expression to extract the node ID with from CSV data")
	String nodeIdRegex;
	
	@Option(names = { "-mode", "--display-mode" },
			description = "how to display the data",
			defaultValue = "PRETTY")
	ResultDisplayMode displayMode = ResultDisplayMode.PRETTY;

	@Parameters(index = "0",
			paramLabel = "<table>",
			description = "the certificate password table, or @file for file to load",
			arity = "0..1")
	String certPasswordTable;
	// @formatter:on

	/**
	 * Constructor.
	 * 
	 * @param reqFactory   the HTTP request factory to use
	 * @param objectMapper the mapper to use
	 */
	public ReportCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		Pattern nodeIdPattern = null;
		if (nodeIdRegex != null && !nodeIdRegex.isBlank()) {
			try {
				nodeIdPattern = Pattern.compile(nodeIdRegex, Pattern.CASE_INSENSITIVE);
			} catch (Exception e) {
				System.err.println("Node ID regex syntax error: %s".formatted(e.getMessage()));
				return 1;
			}
		}

		if (certPasswordTable == null || certPasswordTable.isBlank()) {
			// try stdin
			if (!SystemUtils.systemConsoleIsTerminal()) {
				certPasswordTable = copyToString(new InputStreamReader(System.in, UTF_8));
			}
		} else if (certPasswordTable.startsWith("@")) {
			Path metaPath = Paths.get(certPasswordTable.substring(1));
			if (!Files.isReadable(metaPath)) {
				System.err.println("Certificate password file [%s] not available.".formatted(metaPath));
				return 1;
			}
			certPasswordTable = copyToString(Files.newBufferedReader(metaPath, UTF_8));
		}

		if (certPasswordTable == null || certPasswordTable.isBlank()) {
			System.err.println(
					"No certificate password table provided. Pass table on the command line or standard input.");
			return 1;
		}

		final ZoneId zone = ZoneId.systemDefault();

		try {
			final SortedMap<Long, String> certPasswords = certPasswords(certPasswordTable, nodeIdPattern);
			if (certPasswords.isEmpty()) {
				System.err.println("Certificate password table is empty.");
				return 1;
			}

			// list all available nodes
			final RestClient restClient = restClient();
			SequencedCollection<Long> allNodeIds = listNodeIds(restClient);
			if (allNodeIds.isEmpty()) {
				System.err.println("No node IDs available.");
				return 1;
			}

			List<NodeCertificateInfo> infos = new ArrayList<>(allNodeIds.size());
			try (ProgressBar pb = new ProgressBar("Processing", allNodeIds.size())) {
				for (Long nodeId : allNodeIds) {
					infos.add(nodeCertificateDetails(restClient, nodeId, certPasswords.get(nodeId)));
					pb.step();
				}
			}

			List<?> tableData = (displayMode == ResultDisplayMode.JSON ? infos
					: infos.stream().map(info -> reportRow(info, zone)).toList());
			TableUtils.renderTableData(reportColumns(), tableData, displayMode, objectMapper,
					TableUtils.TableDataJsonPrettyPrinter.INSTANCE, System.out);

			return 0;
		} catch (Exception e) {
			System.err.println("Error creating node certificate report: %s".formatted(e.getMessage()));
		}
		return 1;
	}

	private SortedMap<Long, String> certPasswords(String tableData, Pattern nodeIdPattern) throws IOException {
		if (tableData.startsWith("[")) {
			// treat as JSON
			return certPasswordsJsonArray(tableData);
		} else {
			// treat as CSV
			return certPasswordsCsv(tableData, nodeIdPattern);
		}
	}

	private Long nodeIdValue(String val, Pattern nodeIdPattern) {
		if (val == null) {
			return null;
		}
		if (nodeIdPattern != null) {
			Matcher m = nodeIdPattern.matcher(val);
			if (m.find()) {
				if (m.groupCount() < 1) {
					throw new IllegalStateException(
							"The node ID regex must define a capture group for the node ID value.");
				}
				try {
					return Long.valueOf(m.group(1));
				} catch (NumberFormatException e) {
					return null;
				}
			}
			return null;
		}
		try {
			return Long.valueOf(val);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private SortedMap<Long, String> certPasswordsCsv(final String csv, Pattern nodeIdPattern) throws IOException {
		final SortedMap<Long, String> result = new TreeMap<>();
		try (ICsvListReader csvReader = new CsvListReader(new StringReader(csv), CsvPreference.STANDARD_PREFERENCE)) {
			String[] header = csvReader.getHeader(true);
			int nodeIdCol = -1;
			int passCol = -1;
			for (int i = 0; i < header.length && (nodeIdCol < 0 || passCol < 0); i++) {
				String name = header[i];
				if (name == null || name.isBlank()) {
					continue;
				}
				if (nodeIdCol < 0 && name.equalsIgnoreCase(nodeIdColumnName)) {
					nodeIdCol = i;
				} else if (passCol < 0 && name.equalsIgnoreCase(passwordColumnName)) {
					passCol = i;
				}
			}
			if (nodeIdCol < 0 && header.length != 2) {
				throw new IllegalStateException(
						"Node ID column [%s] not available in CSV data.".formatted(nodeIdColumnName));
			} else if (passCol < 0 && header.length != 2) {
				throw new IllegalStateException(
						"Certificate password [%s] column not available in CSV data.".formatted(passwordColumnName));
			}
			for (List<String> row = csvReader.read(); row != null; row = csvReader.read()) {
				if (nodeIdCol < 0 || passCol < 0) {
					for (int i = 0; i < row.size(); i++) {
						String val = row.get(i);
						if (nodeIdCol < 0 && nodeIdValue(val, nodeIdPattern) != null) {
							nodeIdCol = i;
						}
						if (passCol < 0) {
							passCol = i;
						}
					}
					if (nodeIdCol < 0) {
						throw new IllegalStateException("Node ID column not discovererd in CSV data.");
					} else if (passCol < 0) {
						throw new IllegalStateException("Certificate password column not discovered in CSV data.");
					}
				}
				Long nodeId = (nodeIdCol < row.size() ? nodeIdValue(row.get(nodeIdCol), nodeIdPattern) : null);
				String pass = (passCol < row.size() ? row.get(passCol) : null);
				if (nodeId != null && pass != null) {
					result.put(nodeId, pass);
				}
			}
		}
		return result;
	}

	private SortedMap<Long, String> certPasswordsJsonArray(final String json) throws IOException {
		final JsonNode root = objectMapper.readTree(json);
		final SortedMap<Long, String> result = new TreeMap<>();
		for (JsonNode el : root) {
			long nodeId = 0;
			String pass = null;
			if (el.isArray()) {
				nodeId = el.path(0).asLong();
				pass = el.path(1).textValue();
			} else {
				nodeId = el.path(nodeIdColumnName).asLong();
				pass = el.path(passwordColumnName).textValue();
			}
			if (nodeId > 0 && pass != null) {
				result.put(nodeId, pass);
			}
		}
		return result;
	}

	/**
	 * Get certificate info tabular structure columns.
	 * 
	 * @return the columns
	 */
	public static Column[] reportColumns() {
		// @formatter:off
		return new Column[] {
				new Column().header("Node ID").dataAlign(RIGHT),
				new Column().header("Status").dataAlign(LEFT),
				new Column().header("Valid From").dataAlign(LEFT),
				new Column().header("Valid Until").dataAlign(LEFT),
				new Column().header("Renew After").dataAlign(LEFT),
				new Column().header("Renew Days").dataAlign(RIGHT),
			};
		// @formatter:on
	}

	/**
	 * Convert certificate info into a tabular structure.
	 * 
	 * @param m the info to convert
	 * @return the metadata data
	 */
	public static Object[] reportRow(NodeCertificateInfo m, ZoneId zone) {
		// @formatter:off
		return new Object[] {
				m.nodeId(),
				m.status(),
				(m.validFromDate() != null ? m.validFromDate().atZone(zone).toLocalDateTime() : null),
				(m.validUntilDate() != null ? m.validUntilDate().atZone(zone).toLocalDateTime() : null),
				(m.renewAfterDate() != null ? m.renewAfterDate().atZone(zone).toLocalDateTime() : null),
				(m.renewAfterDate() != null ? m.daysUntilRenewAfterDate(zone) : null),
			};
		// @formatter:on
	}

	/**
	 * Get node certificate details.
	 * 
	 * @param restClient the REST client
	 * @param nodeId     the node ID
	 * @param password   the certificate password
	 * @return the certificate info
	 */
	public static NodeCertificateInfo nodeCertificateDetails(RestClient restClient, Long nodeId, String password) {
		assert nodeId != null;
		if (password == null) {
			return NodeCertificateInfo.missingPassword(nodeId);
		}
		MultiValueMap<String, Object> postBody = new LinkedMultiValueMap<>(1);
		postBody.add("password", password);
		try {
			// @formatter:off
			return restClient.post()
				.uri("/solaruser/api/v1/sec/nodes/cert/" +nodeId)
				.body(postBody)
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.body(NodeCertificateInfo.class)
				;		
			// @formatter:on
		} catch (HttpClientErrorException e) {
			if (e.getStatusCode().isSameCodeAs(HttpStatus.FORBIDDEN)) {
				JsonNode response = e.getResponseBodyAs(JsonNode.class);
				String msg = response.path("message").asText();
				if (msg.toLowerCase(Locale.ENGLISH).contains("password")) {
					return NodeCertificateInfo.invalidPassword(nodeId);
				}
				return NodeCertificateInfo.forbidden(nodeId);
			}
			return NodeCertificateInfo.error(nodeId, e.getMessage());
		}
	}

}
