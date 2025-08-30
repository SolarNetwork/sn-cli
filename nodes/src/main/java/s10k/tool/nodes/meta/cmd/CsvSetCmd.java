/**
 * 
 */
package s10k.tool.nodes.meta.cmd;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.util.FileCopyUtils.copyToString;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.solarnetwork.domain.KeyValuePair;
import net.solarnetwork.domain.datum.GeneralDatumMetadata;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.util.SystemUtils;

/**
 * Write a CSV table to metadata.
 */
@Component
@Command(name = "csv-set")
public class CsvSetCmd extends BaseSubCmd<NodeMetadataCmd> implements Callable<Integer> {

	@Option(names = { "-node", "--node-id" }, description = "a node ID to write metadata to", required = true)
	Long nodeId;

	@Option(names = { "-path", "--path" }, description = "metadata path to CSV data to read")
	String metadataPath;

	@Option(names = { "-s", "--string" }, description = "encode the CSV as a JSON string, intead of JSON arrays")
	boolean encodeAsString;

	@Parameters(index = "0", paramLabel = "<csv>", description = "the CSV to save as metadata, or @file for file to load", arity = "0..1")
	String value;

	/**
	 * Constructor.
	 * 
	 * @param reqFactory   the HTTP request factory to use
	 * @param objectMapper the mapper to use
	 */
	public CsvSetCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		if (value == null || value.isBlank()) {
			// try stdin
			if (!SystemUtils.systemConsoleIsTerminal()) {
				value = copyToString(new InputStreamReader(System.in, UTF_8));
			}
		} else if (value.startsWith("@")) {
			Path metaPath = Paths.get(value.substring(1));
			if (!Files.isReadable(metaPath)) {
				System.err.println("Metadata file [%s] not available.".formatted(metaPath));
				return 1;
			}
			value = copyToString(Files.newBufferedReader(metaPath, UTF_8));
		}

		if (value == null || value.isBlank()) {
			System.err.println("No metadata provided. Pass metadata on the command line or standard input.");
			return 1;
		}

		GeneralDatumMetadata gdm = new GeneralDatumMetadata();
		gdm.populate(new KeyValuePair[] { new KeyValuePair(metadataPath, value) });
		if (!encodeAsString) {
			// convert CSV string into String[][]-like structure
			List<List<String>> data = csvData(value);
			int lastSepIdx = metadataPath.lastIndexOf('/');
			String parentMetadataPath = metadataPath.substring(0, lastSepIdx);
			String csvPropertyKey = metadataPath.substring(lastSepIdx + 1);
			Object parent = gdm.metadataAtPath(parentMetadataPath);
			if (parent instanceof Map<?, ?> map) {
				@SuppressWarnings("unchecked")
				Map<String, Object> parentMap = (Map<String, Object>) map;
				parentMap.put(csvPropertyKey, data);
			}
		}

		final RestClient restClient = restClient();
		try {
			SaveNodeMetadataCmd.saveMetadata(restClient, nodeId, gdm, false);
			System.out.println("CSV saved to to path [%s].".formatted(metadataPath));
			return 0;
		} catch (Exception e) {
			System.err.println("Error listing node metadata: %s".formatted(e.getMessage()));
		}

		return 1;
	}

	private List<List<String>> csvData(String csv) throws IOException {
		List<List<String>> result = new ArrayList<>();
		try (ICsvListReader csvReader = new CsvListReader(new StringReader(csv), CsvPreference.STANDARD_PREFERENCE)) {
			for (List<String> row = csvReader.read(); row != null; row = csvReader.read()) {
				result.add(row);
			}
		}
		return result;
	}

}
