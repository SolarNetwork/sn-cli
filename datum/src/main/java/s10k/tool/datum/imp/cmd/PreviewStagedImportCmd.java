package s10k.tool.datum.imp.cmd;

import static java.util.stream.StreamSupport.stream;
import static org.springframework.util.StreamUtils.nonClosing;
import static s10k.tool.common.util.RestUtils.checkSuccess;

import java.util.List;
import java.util.concurrent.Callable;

import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.freva.asciitable.Column;

import net.solarnetwork.domain.datum.Datum;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.util.DatumUtils;
import s10k.tool.common.util.DatumUtils.DatumResultStructure;
import s10k.tool.common.util.TableUtils;

/**
 * Preview a staged datum import.
 */
@Command(name = "preview-staged", aliases = "preview", sortSynopsis = false, descriptionHeading = "%n", optionListHeading = "%n", description = {
		"Preview a sample of the datum included in @|bold staged|@ datum import job.%n" })
public class PreviewStagedImportCmd extends BaseSubCmd<DatumImportsCmd> implements Callable<Integer> {

	// @formatter:off
	@Option(names = { "-j", "--job-id" },
			description = "the staged job to preview",
			required = true)
	String jobId;

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
	public PreviewStagedImportCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		final RestClient restClient = restClient();
		try {
			final List<Datum> datum = previewDatumImportTasks(restClient, objectMapper, jobId);

			if (datum.isEmpty()) {
				System.err.println("No datum generated.");
				return 0;
			}

			if (displayMode == ResultDisplayMode.JSON) {
				objectMapper.writeValue(nonClosing(System.out), datum);
			} else {
				final DatumResultStructure structure = DatumUtils.resultStructure(datum);
				List<?> tableData = stream(datum.spliterator(), false).map(d -> structure.tableDataRow(d)).toList();
				TableUtils.renderTableData(structure != null ? structure.columns().toArray(Column[]::new) : null,
						tableData, displayMode, objectMapper, TableUtils.TableDataJsonPrettyPrinter.INSTANCE,
						System.out);
			}
			return 0;
		} catch (Exception e) {
			System.err.println("Error listing datum: %s".formatted(e.getMessage()));
		}
		return 1;
	}

	/**
	 * List datum import tasks.
	 * 
	 * @param restClient   the REST client
	 * @param objectMapper the object mapper
	 * @param jobId        the staged job ID to preview
	 * @return the list of matching tasks
	 * @throws IllegalStateException if an error occurs
	 */
	public static List<Datum> previewDatumImportTasks(RestClient restClient, ObjectMapper objectMapper, String jobId) {
		// @formatter:off
		final JsonNode response = checkSuccess(restClient.get()
				.uri(b -> {
					b.path("/solaruser/api/v1/sec/user/import/jobs/{jobId}/preview");
					return b.build(jobId);
				})
			.accept(MediaType.APPLICATION_JSON)
			.retrieve()
			.body(JsonNode.class)
			);		
		// @formatter:on

		try {
			Datum[] result = objectMapper.treeToValue(response.path("data").path("results"), Datum[].class);
			return (result != null ? List.of(result) : List.of());
		} catch (JsonProcessingException | IllegalArgumentException e) {
			throw new IllegalStateException("Error parsing datum import task list response: " + e.getMessage(), e);
		}
	}

}
