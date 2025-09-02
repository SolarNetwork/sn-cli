/**
 * 
 */
package s10k.tool.nodes.meta.cmd;

import static s10k.tool.nodes.meta.cmd.ListNodeMetadataCmd.listNodeMetadata;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.solarnetwork.domain.datum.GeneralDatumMetadata;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.util.TableUtils;
import s10k.tool.nodes.domain.NodeMetadata;

/**
 * Read metadata as a CSV table.
 */
@Component
@Command(name = "csv-get", aliases = "csv")
public class CsvGetCmd extends BaseSubCmd<NodeMetadataCmd> implements Callable<Integer> {

	@Option(names = { "-node", "--node-id" }, description = "a node ID to get metadata from", required = true)
	Long nodeId;

	@Option(names = { "-path", "--path" }, description = "metadata path to CSV data to read")
	String metadataPath;

	@Option(names = { "-mode",
			"--display-mode" }, description = "how to display the CSV data", required = false, defaultValue = "PRETTY")
	ResultDisplayMode displayMode = ResultDisplayMode.PRETTY;

	/**
	 * Constructor.
	 * 
	 * @param reqFactory   the HTTP request factory to use
	 * @param objectMapper the mapper to use
	 */
	public CsvGetCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		final RestClient restClient = restClient();

		try {
			List<NodeMetadata> metas = listNodeMetadata(restClient, objectMapper, new Long[] { nodeId }, null);
			if (metas.isEmpty()) {
				System.out.println("No metadata matched your criteria.");
				return 0;
			}

			GeneralDatumMetadata gdm = metas.getFirst().metadata();
			if (gdm == null || !gdm.hasMetadataAtPath(metadataPath)) {
				System.out.println("No metadata available at path [%s]. Available metadata:".formatted(metadataPath));
				System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(gdm));
				return 0;
			}

			List<List<String>> data = parseCsvMetadata(gdm.metadataAtPath(metadataPath));
			TableUtils.renderTableData(data, displayMode, objectMapper, System.out);

			return 0;
		} catch (Exception e) {
			System.err.println("Error listing node metadata: %s".formatted(e.getMessage()));
		}
		return 1;
	}

	private static List<List<String>> parseCsvMetadata(Object csvMeta) throws IOException {
		List<List<String>> result = new ArrayList<>();
		if (csvMeta instanceof String s) {
			try (ICsvListReader csvReader = new CsvListReader(new StringReader(s), CsvPreference.STANDARD_PREFERENCE)) {
				String[] headers = csvReader.getHeader(true);
				result.add(Arrays.asList(headers));
				for (List<String> row = csvReader.read(); row != null; row = csvReader.read()) {
					result.add(row);
				}
			}
		} else if (csvMeta instanceof List<?> list) {
			for (Object row : list) {
				if (row instanceof List<?> rowList) {
					result.add(rowList.stream().map(e -> e != null ? e.toString() : null).toList());
				}
			}
		}
		return result;
	}

}
