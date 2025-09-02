package s10k.tool.datum.stream.cmd;

import static com.github.freva.asciitable.HorizontalAlign.LEFT;
import static com.github.freva.asciitable.HorizontalAlign.RIGHT;
import static net.solarnetwork.domain.datum.DatumSamplesType.Accumulating;
import static net.solarnetwork.domain.datum.DatumSamplesType.Instantaneous;
import static net.solarnetwork.domain.datum.DatumSamplesType.Status;
import static org.springframework.util.StringUtils.arrayToCommaDelimitedString;
import static s10k.tool.common.util.RestUtils.checkSuccess;
import static s10k.tool.common.util.RestUtils.populateQueryParameters;
import static s10k.tool.common.util.StringUtils.naturallyCaseInsensitiveSorted;

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
import com.github.freva.asciitable.Column;

import net.solarnetwork.domain.datum.ObjectDatumKind;
import net.solarnetwork.domain.datum.ObjectDatumStreamMetadata;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.util.TableUtils;
import s10k.tool.datum.domain.DatumStreamFilter;

/**
 * View datum stream metadata matching a search criteria.
 */
@Component
@Command(name = "list", sortSynopsis = false)
public class ListDatumStreamMetadataCmd extends BaseSubCmd<DatumStreamCmd> implements Callable<Integer> {

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
	
	@Option(names = { "-prop", "--property" },
			description = "a property name to require in returned metadata",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "propName")
	String[] propertyNames;
	
	@Option(names = { "-i", "--instantaneous" },
			description = "an instantaneous property name to require in returned metadata",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "propName")
	String[] instantaneousPropertyNames;
	
	@Option(names = { "-a", "--accumulating" },
			description = "an accumulating property name to require in returned metadata",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "propName")
	String[] accumulatingPropertyNames;
	
	@Option(names = { "-s", "--status" },
			description = "a status property name to require in returned metadata",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "propName")
	String[] statusPropertyNames;
	
	@Option(names = { "-mode", "--display-mode" },
			description = "how to display the data",
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
	public ListDatumStreamMetadataCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		final DatumStreamFilter filter = DatumStreamFilter
				.datumStreamFilter(streamIds,
						nodeOrLocationIds != null ? nodeOrLocationIds.isLocation() ? nodeOrLocationIds.locationIds
								: nodeOrLocationIds.nodeIds : null,
						sourceIds, propertyNames, instantaneousPropertyNames, accumulatingPropertyNames,
						statusPropertyNames);

		final RestClient restClient = restClient();

		try {
			List<ObjectDatumStreamMetadata> metas = listStreamMetadata(restClient, objectMapper,
					nodeOrLocationIds != null && nodeOrLocationIds.isLocation() ? ObjectDatumKind.Location
							: ObjectDatumKind.Node,
					filter);
			if (metas.isEmpty()) {
				System.err.println("No stream metadata matched your criteria.");
				return 0;
			}

			List<?> tableData = (displayMode == ResultDisplayMode.JSON ? metas
					: metas.stream().map(ListDatumStreamMetadataCmd::metadataRow).toList());
			// @formatter:off
			TableUtils.renderTableData(new Column[] {
					new Column().header("Stream ID").dataAlign(LEFT),
					new Column().header("Kind").dataAlign(LEFT),
					new Column().header("ID").dataAlign(RIGHT),
					new Column().header("Source ID").dataAlign(LEFT),
					new Column().header("Time Zone").dataAlign(LEFT),
					new Column().header("Instantaneous").dataAlign(LEFT),
					new Column().header("Accumulating").dataAlign(LEFT),
					new Column().header("Status").dataAlign(LEFT),	
				}, tableData, displayMode, objectMapper, TableUtils.TableDataJsonPrettyPrinter.INSTANCE, System.out);
			// @formatter:on
			return 0;
		} catch (Exception e) {
			System.err.println("Error viewing datum stream metadata: %s".formatted(e.getMessage()));
		}
		return 1;
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
				populateQueryParameters(b, () -> filter.toRequestMap(kind));
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
