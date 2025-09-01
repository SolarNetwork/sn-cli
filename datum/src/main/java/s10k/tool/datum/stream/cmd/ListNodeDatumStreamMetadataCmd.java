package s10k.tool.datum.stream.cmd;

import static com.github.freva.asciitable.HorizontalAlign.LEFT;
import static com.github.freva.asciitable.HorizontalAlign.RIGHT;
import static net.solarnetwork.domain.datum.DatumSamplesType.Accumulating;
import static net.solarnetwork.domain.datum.DatumSamplesType.Instantaneous;
import static net.solarnetwork.domain.datum.DatumSamplesType.Status;
import static org.springframework.util.StringUtils.arrayToCommaDelimitedString;
import static s10k.tool.common.util.RestUtils.checkSuccess;
import static s10k.tool.common.util.StringUtils.naturallyCaseInsensitiveSorted;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;

import net.solarnetwork.domain.datum.ObjectDatumKind;
import net.solarnetwork.domain.datum.ObjectDatumStreamMetadata;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.domain.TableDisplayMode;
import s10k.tool.common.util.TableUtils;
import s10k.tool.datum.domain.DatumStreamFilter;

/**
 * View datum stream metadata matching a search criteria.
 */
@Component
@Command(name = "list")
public class ListNodeDatumStreamMetadataCmd extends BaseSubCmd<DatumStreamCmd> implements Callable<Integer> {

	// @formatter:off
	@Option(names = { "-stream", "--stream-id" },
			description = "a stream ID to view metadata for",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "streamId")
	UUID streamIds[];
	
	@ArgGroup(exclusive = true, multiplicity = "0..1")
	NodeOrLocationIds nodeOrLocationIds;

	@Option(names = { "-source", "--source-id" },
			description = "a source ID to return metadata for",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "sourceId")
	String[] sourceIds;
	
	@Option(names = { "-mode", "--display-mode" },
			description = "how to display the CSV data",
			defaultValue = "PRETTY")
	ResultDisplayMode displayMode = ResultDisplayMode.PRETTY;
	// @formatter:on

	/**
	 * Grouping of node/location IDs, where only one or the other should be
	 * specified.
	 */
	static class NodeOrLocationIds {
		// @formatter:off
    	@Option(names = { "-node", "--node-id" },
    			description = "a node ID to return metadata for",
    			split = "\\s*,\\s*",
    			splitSynopsisLabel = ",",
    			paramLabel = "nodeId")
    	Long[] nodeIds;

    	@Option(names = { "-loc", "--location-id" },
    			description = "a location ID to return metadata for",
    			split = "\\s*,\\s*",
    			splitSynopsisLabel = ",",
    			paramLabel = "locId")
    	Long[] locationIds;
    	// @formatter:on

		/**
		 * Test if location IDs are provided (otherwise node IDs are).
		 * 
		 * @return {@code true} if location IDs are configured, {@code false} if node
		 *         IDs are
		 */
		boolean isLocation() {
			return locationIds != null && locationIds.length > 0;
		}

	}

	/**
	 * Constructor.
	 * 
	 * @param reqFactory   the HTTP request factory to use
	 * @param objectMapper the mapper to use
	 */
	public ListNodeDatumStreamMetadataCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		if ((streamIds == null || streamIds.length < 1)
				&& (nodeOrLocationIds == null
						|| ((nodeOrLocationIds.nodeIds == null || nodeOrLocationIds.nodeIds.length < 1)
								&& (nodeOrLocationIds.locationIds == null || nodeOrLocationIds.locationIds.length < 1)))
				&& (sourceIds == null || sourceIds.length < 1)) {
			System.err.println("Must provide at least one query filter option.");
			return 1;
		}

		final RestClient restClient = restClient();

		final DatumStreamFilter filter = DatumStreamFilter.datumStreamFilter(streamIds,
				nodeOrLocationIds != null
						? nodeOrLocationIds.isLocation() ? nodeOrLocationIds.locationIds : nodeOrLocationIds.nodeIds
						: null,
				sourceIds);

		try {
			List<ObjectDatumStreamMetadata> metas = listStreamMetadata(restClient, objectMapper,
					nodeOrLocationIds.isLocation() ? ObjectDatumKind.Location : ObjectDatumKind.Node, filter);
			if (metas.isEmpty()) {
				System.out.println("No stream metadata matched your criteria.");
				return 0;
			}

			if (displayMode == ResultDisplayMode.PRETTY) {
				// @formatter:off
				AsciiTable.builder()
					.data(new Column[] {
							new Column().header("Stream ID").dataAlign(LEFT),
							new Column().header("Kind").dataAlign(LEFT),
							new Column().header("ID").dataAlign(RIGHT),
							new Column().header("Source ID").dataAlign(LEFT),
							new Column().header("Time Zone").dataAlign(LEFT),
							new Column().header("Instantaneous").dataAlign(LEFT),
							new Column().header("Accumulating").dataAlign(LEFT),
							new Column().header("Status").dataAlign(LEFT),	
						}, metas.stream().map(ListNodeDatumStreamMetadataCmd::metadataRow).toArray(Object[][]::new))
					.writeTo(System.out)
					;
				// @formatter:on
				System.out.println();
			} else if (displayMode == ResultDisplayMode.CSV) {
				List<Object[]> tableData = new ArrayList<>();
				tableData.add(metadataHeaderRow());
				tableData.addAll(metas.stream().map(ListNodeDatumStreamMetadataCmd::metadataRow).toList());
				TableUtils.renderTableData(tableData, TableDisplayMode.CSV, null, System.out);
			} else {
				// JSON
				objectMapper.writerWithDefaultPrettyPrinter().writeValue(System.out, metas);
				System.out.println();
			}
			return 0;
		} catch (Exception e) {
			System.err.println("Error viewing datum stream metadata: %s".formatted(e.getMessage()));
		}
		return 1;
	}

	/**
	 * Get a datum stream metadata tabular structure header row.
	 * 
	 * @return the header row
	 */
	public static String[] metadataHeaderRow() {
		// @formatter:off
		return new String[] {
				"Stream ID",
				"Kind",
				"ID",
				"Source ID",
				"Time Zone",
				"Instantaneous",
				"Accumulating",
				"Status"
		};
		// @formatter:on
	}

	/**
	 * Convert datum stream metadata into a tabular structure.
	 * 
	 * @param m the metadata to convert
	 * @return the metadata data
	 */
	public static Object[] metadataRow(ObjectDatumStreamMetadata m) {
		// @formatter:off
		return new Object[] {
				m.getStreamId(),
				m.getKind().name(),
				m.getObjectId(),
				m.getSourceId(),
				m.getTimeZoneId(),
				arrayToCommaDelimitedString(naturallyCaseInsensitiveSorted(m.propertyNamesForType(Instantaneous))),
				arrayToCommaDelimitedString(naturallyCaseInsensitiveSorted(m.propertyNamesForType(Accumulating))),
				arrayToCommaDelimitedString(naturallyCaseInsensitiveSorted(m.propertyNamesForType(Status)))
			};
		// @formatter:on
	}

	/**
	 * List stream metadata.
	 * 
	 * @param restClient   the REST client
	 * @param objectMapper the object mapper
	 * @param kind         the datum stream kind to find
	 * @param filter       the search filter
	 * @return the metadata
	 * @throws IllegalStateException if the stream metadata is not available
	 */
	public static List<ObjectDatumStreamMetadata> listStreamMetadata(RestClient restClient, ObjectMapper objectMapper,
			ObjectDatumKind kind, DatumStreamFilter filter) {
		// @formatter:off
		JsonNode response = restClient.get()
			.uri(b -> {
				b.path("/solarquery/api/v1/sec/datum/stream/meta/" + (kind == ObjectDatumKind.Location ? "loc" : "node"));
				MultiValueMap<String, Object> params = filter.toRequestMap(kind);
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

		List<ObjectDatumStreamMetadata> result = new ArrayList<>(response.path("data").size());
		for (JsonNode node : response.path("data")) {
			ObjectDatumStreamMetadata meta;
			try {
				meta = objectMapper.treeToValue(node, ObjectDatumStreamMetadata.class);
			} catch (JsonProcessingException | IllegalArgumentException e) {
				throw new IllegalStateException("Error parsing datum stream metadata response: " + e.getMessage(), e);
			}
			if (meta != null) {
				result.add(meta);
			}
		}
		return result;

	}

}
