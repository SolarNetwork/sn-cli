package s10k.tool.nodes.cmd;

import static com.github.freva.asciitable.HorizontalAlign.LEFT;
import static com.github.freva.asciitable.HorizontalAlign.RIGHT;
import static s10k.tool.common.util.RestUtils.checkSuccess;
import static s10k.tool.common.util.TableUtils.tableConfig;

import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.Callable;

import org.jspecify.annotations.Nullable;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.freva.asciitable.Column;

import net.solarnetwork.domain.SimpleLocation;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.util.RestUtils;
import s10k.tool.common.util.StringUtils;
import s10k.tool.common.util.TableUtils;
import s10k.tool.nodes.domain.NodeFilter;
import s10k.tool.nodes.domain.UserNodeInfo;

/**
 * List nodes.
 */
@Command(name = "list", sortSynopsis = false, showDefaultValues = true, descriptionHeading = "%n", optionListHeading = "%n", description = {
		"List brief node information records matching search criteria.%n" })
public class ListNodesCmd extends BaseSubCmd<NodesCmd> implements Callable<Integer> {

	// @formatter:off
	@Option(names = { "-node", "--node-id" },
			description = "a node ID to match",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "nodeId")
	Long @Nullable [] nodeIds;

	@Option(names = { "-loc", "--location-id" },
			description = "a location ID to match",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "locId")
	Long @Nullable [] locationIds;

	@Option(names = { "-m", "--name" },
			description = "a case-insensitive name or descrition substring to to match",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "name")
	String @Nullable [] names;
	
	@Option(names = { "-c", "--country" },
			description = "a time zone to to match")
	@Nullable String country;
	
	@Option(names = { "-tz", "--time-zone" },
			description = "a time zone to to match")
	@Nullable ZoneId zone;
	
	@Option(names = {"-M", "--max"},
			description = "return at most this many results", paramLabel = "max")
	int maxResults;

	@Option(names = {"-O", "--offset"},
			description = "start returning results from this offset, 0 being the first result")
	long resultOffset;

	@Option(names = { "-sort", "--sort-by" },
			description = "sort the results; one of created, name, node, or zone",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "orderKey")
	String @Nullable [] orderBys;
	
	@Option(names = { "-mode", "--display-mode" },
			description = "how to display the data")
	ResultDisplayMode displayMode;
	// @formatter:on

	/**
	 * Order by keys.
	 */
	public enum OrderBy {
		Created, Name, Node, Zone,
	}

	/**
	 * Constructor.
	 * 
	 * @param reqFactory   the HTTP request factory to use
	 * @param objectMapper the mapper to use
	 */
	public ListNodesCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		try {
			final RestClient restClient = restClient();
			final ObjectMapper objectMapper = objectMapper();
			final ResultDisplayMode displayMode = displayMode(this.displayMode);
			final NodeFilter filter = nodeFilter();
			final List<UserNodeInfo> nodes = listUserNodes(restClient, objectMapper, filter);
			if (nodes.isEmpty()) {
				System.err.println("No nodes available.");
				return 1;
			}

			final List<?> tableData = (displayMode == ResultDisplayMode.JSON ? nodes
					: nodes.stream().map(ListNodesCmd::userNodeInfoRow).toList());
			TableUtils.renderTableData(userNodeInfoColumns(), tableData, tableConfig(this, displayMode, zone),
					objectMapper, TableUtils.TableDataJsonPrettyPrinter.INSTANCE, System.out);
			return 0;
		} catch (Exception e) {
			System.err.println("Error listing node metadata: %s".formatted(e.getMessage()));
		}
		return 1;
	}

	private NodeFilter nodeFilter() {
		final NodeFilter filter = new NodeFilter();
		if (nodeIds != null) {
			filter.setNodeIds(List.of(nodeIds));
		}
		if (locationIds != null) {
			filter.setLocationIds(List.of(locationIds));
		}
		if (names != null) {
			filter.setNames(List.of(names));
		}
		if (country != null) {
			loc(filter).setCountry(country);
		}
		if (zone != null) {
			loc(filter).setTimeZoneId(zone.getId());
		}
		filter.setWithoutTotalResultsCount(true);
		filter.setOrderBy(StringUtils.orderByList(orderBys, OrderBy.class));
		if (maxResults > 0) {
			filter.setMax(maxResults);
		}
		if (resultOffset > 0) {
			filter.setOffset(resultOffset);
		}
		return filter;
	}

	private SimpleLocation loc(NodeFilter filter) {
		SimpleLocation loc = filter.getLocation();
		if (loc == null) {
			loc = new SimpleLocation();
			filter.setLocation(loc);
		}
		return loc;
	}

	/**
	 * List user nodes.
	 * 
	 * @param restClient   the REST client
	 * @param objectMapper the mapper to use
	 * @param filter       the search criteria
	 * @return the nodes
	 */
	public static List<UserNodeInfo> listUserNodes(final RestClient restClient, final ObjectMapper objectMapper,
			final @Nullable NodeFilter filter) {
		// @formatter:off
		final JsonNode response = checkSuccess(restClient.get()
				.uri(b -> {
					b.path("/solaruser/api/v1/sec/nodes/find");
					if (filter != null ) {
					RestUtils.populateQueryParameters(b, filter::toRequestMap);
					}
					return b.build();
				})
			.accept(MediaType.APPLICATION_JSON)
			.retrieve()
			.body(JsonNode.class)
			);
		// @formatter:on

		try {
			UserNodeInfo[] result = objectMapper.treeToValue(response.path("data").path("results"),
					UserNodeInfo[].class);
			return (result != null ? List.of(result) : List.of());
		} catch (JsonProcessingException | IllegalArgumentException e) {
			throw new IllegalStateException("Error parsing user node list response: " + e.getMessage(), e);
		}
	}

	/**
	 * Get node info tabular structure columns.
	 * 
	 * @return the columns
	 */
	public static Column[] userNodeInfoColumns() {
		// @formatter:off
		return new Column[] {
				new Column().header("Node ID").dataAlign(RIGHT),
				new Column().header("Name").dataAlign(LEFT),
				new Column().header("Created").dataAlign(LEFT),
				new Column().header("Location ID").dataAlign(RIGHT),
				new Column().header("Country").dataAlign(LEFT),
				new Column().header("Time Zone").dataAlign(LEFT),
				new Column().header("Public").dataAlign(LEFT),
			};
		// @formatter:on
	}

	/**
	 * Convert node info into a tabular structure.
	 * 
	 * @param info the node info to convert
	 * @return the tabular data
	 */
	public static Object[] userNodeInfoRow(UserNodeInfo info) {
		// @formatter:off
		return new Object[] {
				info.nodeId(),
				info.nameAndDescription("\n"),
				info.created(),
				info.locationId(),
				info.country(),
				info.timeZone(),
				!info.requiresAuthorization(),
			};
		// @formatter:on
	}

}
