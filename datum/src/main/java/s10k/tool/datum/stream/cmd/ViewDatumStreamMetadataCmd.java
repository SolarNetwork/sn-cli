package s10k.tool.datum.stream.cmd;

import static com.github.freva.asciitable.HorizontalAlign.LEFT;
import static net.solarnetwork.domain.datum.DatumSamplesType.Accumulating;
import static net.solarnetwork.domain.datum.DatumSamplesType.Instantaneous;
import static net.solarnetwork.domain.datum.DatumSamplesType.Status;
import static net.solarnetwork.domain.datum.ObjectDatumKind.Location;
import static net.solarnetwork.domain.datum.ObjectDatumKind.Node;
import static org.springframework.util.StreamUtils.nonClosing;
import static org.springframework.util.StringUtils.arrayToCommaDelimitedString;
import static s10k.tool.common.util.RestUtils.checkSuccess;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;

import net.solarnetwork.domain.datum.ObjectDatumKind;
import net.solarnetwork.domain.datum.ObjectDatumStreamMetadata;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.domain.TableDisplayMode;
import s10k.tool.common.util.SystemUtils;
import s10k.tool.common.util.TableUtils;

/**
 * View datum stream metadata matching a search criteria.
 */
@Component
@Command(name = "view")
public class ViewDatumStreamMetadataCmd extends BaseSubCmd<DatumStreamCmd> implements Callable<Integer> {

	// @formatter:off
	@Option(names = { "-stream", "--stream-id" }, description = "the stream ID to view metadata for", required = true)
	UUID streamId;
	
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
	public ViewDatumStreamMetadataCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		final RestClient restClient = restClient();

		// try node stream first, and if not found try location stream
		try {
			ObjectDatumStreamMetadata meta = viewStreamMetadata(restClient, objectMapper, Node, streamId);
			if (meta == null) {
				meta = viewStreamMetadata(restClient, objectMapper, Location, streamId);
				if (meta == null) {
					System.err.println("Datum stream not available.");
					return 1;
				}
			}

			if (displayMode == ResultDisplayMode.PRETTY) {
				// @formatter:off
				AsciiTable.builder()
					.data(new Column[] {
							new Column().header("Property").dataAlign(LEFT),
							new Column().header("Value").dataAlign(LEFT),
						}, new Object[][] {
						new Object[] { "Stream ID", meta.getStreamId() },
						new Object[] { "Kind", meta.getKind().name() },
						new Object[] { "ID", meta.getObjectId() },
						new Object[] { "Source ID", meta.getSourceId() },
						new Object[] { "Time Zone", meta.getTimeZoneId() },
						new Object[] { "Instantaneous", arrayToCommaDelimitedString(meta.propertyNamesForType(Instantaneous)) },
						new Object[] { "Accumulating", arrayToCommaDelimitedString(meta.propertyNamesForType(Accumulating)) },
						new Object[] { "Status", arrayToCommaDelimitedString(meta.propertyNamesForType(Status)) },
					})
					.writeTo(System.out)
					;
				// @formatter:on
			} else if (displayMode == ResultDisplayMode.CSV) {
				List<Object[]> tableData = new ArrayList<>();
				tableData.add(ListDatumStreamMetadataCmd.metadataHeaderRow());
				tableData.add(ListDatumStreamMetadataCmd.metadataRow(meta));
				TableUtils.renderTableData(tableData, TableDisplayMode.CSV, null, System.out);
			} else {
				// JSON
				objectMapper.writerWithDefaultPrettyPrinter().writeValue(nonClosing(System.out), meta);
				if (SystemUtils.systemConsoleIsTerminal()) {
					System.out.println();
				}
			}
			return 0;
		} catch (Exception e) {
			System.err.println("Error viewing datum stream metadata: %s".formatted(e.getMessage()));
		}
		return 1;
	}

	/**
	 * View stream metadata.
	 * 
	 * @param restClient   the REST client
	 * @param objectMapper the object mapper
	 * @param kind         the datum stream kind to find
	 * @param streamId     the stream ID to view
	 * @return the metadata
	 * @throws IllegalStateException if the stream metadata is not available
	 */
	public static ObjectDatumStreamMetadata viewStreamMetadata(RestClient restClient, ObjectMapper objectMapper,
			ObjectDatumKind kind, UUID streamId) {
		assert streamId != null;
		// @formatter:off
		JsonNode response = restClient.get()
				.uri(b -> {
					b.path("/solaruser/api/v1/sec/datum/stream/meta/{kind}/{streamId}");
					return b.build(kind == Location ? "loc" : "node", streamId);
				})
			.accept(MediaType.APPLICATION_JSON)
			.retrieve()
			.body(JsonNode.class)
			;		
		// @formatter:on

		checkSuccess(response);

		try {
			return objectMapper.treeToValue(response.path("data"), ObjectDatumStreamMetadata.class);
		} catch (JsonProcessingException | IllegalArgumentException e) {
			throw new IllegalStateException("Error parsing datum stream metadata response: " + e.getMessage(), e);
		}
	}

}
