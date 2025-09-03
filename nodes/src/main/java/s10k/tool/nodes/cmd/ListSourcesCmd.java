package s10k.tool.nodes.cmd;

import static com.github.freva.asciitable.HorizontalAlign.LEFT;
import static com.github.freva.asciitable.HorizontalAlign.RIGHT;
import static s10k.tool.common.util.RestUtils.checkSuccess;
import static s10k.tool.common.util.RestUtils.populateQueryParameters;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.freva.asciitable.Column;

import net.solarnetwork.domain.datum.ObjectDatumKind;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.ObjectAndSource;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.domain.SourceFilter;
import s10k.tool.common.util.DateUtils;
import s10k.tool.common.util.LocalDateTimeConverter;
import s10k.tool.common.util.TableUtils;

/**
 * View datum stream metadata IDs matching a search criteria.
 */
@Component
@Command(name = "sources", sortSynopsis = false)
public class ListSourcesCmd extends BaseSubCmd<NodesCmd> implements Callable<Integer> {

	// @formatter:off	
	@Option(names = { "-node", "--node-id" },
			description = "a node ID to return metadata for",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "nodeId")
	Long[] nodeIds;

	@Option(names = { "-source", "--source-id" },
			description = "a source ID to return metadata for",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "sourceId")
	String[] sourceIds;
	
	@Option(names = { "-min", "--min-date" },
			description = "a minimum datum date after which a source must have datum to match",
			converter = LocalDateTimeConverter.class)
	LocalDateTime minDate;

	@Option(names = { "-max", "--max-date" },
			description = "a maximum datum date (exclusive) before which a source must have datum to match",
			converter = LocalDateTimeConverter.class)
	LocalDateTime maxDate;
	
	@Option(names = {"-local", "--local-dates"},
			description = "treat the min/max dates as 'node local' dates, instead of UTC (or local time zone when -tz used)")
	boolean useLocalDates;

	@Option(names = { "-tz", "--time-zone" },
			description = "a time zone to interpret the min and max dates as, instead of the local time zone")
	ZoneId zone;
	
	@Option(names = { "-filter", "--filter" }, description = "a metadata filter")
	String metadataFilter;

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
	 * Constructor.
	 * 
	 * @param reqFactory   the HTTP request factory to use
	 * @param objectMapper the mapper to use
	 */
	public ListSourcesCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		// @formatter:off
		final SourceFilter filter = SourceFilter.sourceFilter(
				nodeIds,
				sourceIds,
				DateUtils.zonedDate(maxDate, zone),
				DateUtils.zonedDate(maxDate, zone),
				useLocalDates,
				metadataFilter,
				propertyNames,
				instantaneousPropertyNames,
				accumulatingPropertyNames,
				statusPropertyNames
		);
		// @formatter:on

		final RestClient restClient = restClient();

		try {
			List<ObjectAndSource> sources = listSources(restClient, objectMapper, ObjectDatumKind.Node, filter);
			if (sources.isEmpty()) {
				System.err.println("No sources matched your criteria.");
				return 0;
			}

			List<?> tableData = (displayMode == ResultDisplayMode.JSON ? sources
					: sources.stream().map(ListSourcesCmd::tableDataRow).toList());
			// @formatter:off
			TableUtils.renderTableData(new Column[] {
					new Column().header("Node ID").dataAlign(RIGHT),
					new Column().header("Source ID").dataAlign(LEFT),
				}, tableData, displayMode, objectMapper, TableUtils.TableDataJsonPrettyPrinter.INSTANCE, System.out);
			// @formatter:on
			return 0;
		} catch (Exception e) {
			System.err.println("Error viewing node sources: %s".formatted(e.getMessage()));
		}
		return 1;
	}

	/**
	 * Convert a {@code NodeAndSource} into a tabular structure.
	 * 
	 * @param nodeAndSource the object to convert
	 * @return the tabular data
	 */
	public static Object[] tableDataRow(ObjectAndSource nodeAndSource) {
		// @formatter:off
		return new Object[] {
				nodeAndSource.objectId(),
				nodeAndSource.sourceId(),
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
	public static List<ObjectAndSource> listSources(RestClient restClient, ObjectMapper objectMapper,
			ObjectDatumKind kind, SourceFilter filter) {
		// @formatter:off
		JsonNode response = restClient.get()
			.uri(b -> {
				b.path("/solarquery/api/v1/sec/nodes/sources");
				populateQueryParameters(b, filter::toNodeRequestMap);
				return b.build();
			})
			.accept(MediaType.APPLICATION_JSON)
			.retrieve()
			.body(JsonNode.class)
			;		
		// @formatter:on

		checkSuccess(response);

		List<ObjectAndSource> result = new ArrayList<>(response.path("data").size());
		for (JsonNode node : response.path("data")) {
			JsonNode tmp = node.path("nodeId");
			if (tmp.isNumber()) {
				var objSource = new ObjectAndSource(ObjectDatumKind.Node, tmp.longValue(),
						node.path("sourceId").textValue());
				if (objSource.isValid()) {
					result.add(objSource);
				}
			}
		}
		return result;

	}

}
