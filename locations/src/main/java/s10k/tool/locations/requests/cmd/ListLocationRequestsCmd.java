package s10k.tool.locations.requests.cmd;

import static com.github.freva.asciitable.HorizontalAlign.LEFT;
import static com.github.freva.asciitable.HorizontalAlign.RIGHT;
import static net.solarnetwork.util.CollectionUtils.getMapString;
import static net.solarnetwork.util.CollectionUtils.mapPropertyStringList;
import static net.solarnetwork.util.CollectionUtils.mapPropertyStringMap;
import static s10k.tool.common.util.RestUtils.checkSuccess;
import static s10k.tool.common.util.TableUtils.tableConfig;

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

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.util.RestUtils;
import s10k.tool.common.util.TableUtils;
import s10k.tool.locations.domain.LocationFilter;
import s10k.tool.locations.domain.LocationRequest;
import s10k.tool.locations.domain.LocationRequestStatus;

/**
 * List location requests.
 */
@Command(name = "list", sortSynopsis = false, showDefaultValues = true, descriptionHeading = "%n", optionListHeading = "%n", description = {
		"List location request records matching search criteria.%n" })
public class ListLocationRequestsCmd extends BaseSubCmd<LocationsRequestsGroup> implements Callable<Integer> {

	// @formatter:off
	@Option(names = { "-s", "--status" },
			description = "a status to match",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "status")
	LocationRequestStatus @Nullable [] statuses;
	
	@Option(names = { "-mode", "--display-mode" },
			description = "how to display the data")
	@Nullable ResultDisplayMode displayMode;
	// @formatter:on

	/**
	 * Constructor.
	 * 
	 * @param reqFactory   the HTTP request factory to use
	 * @param objectMapper the mapper to use
	 */
	public ListLocationRequestsCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		try {
			final RestClient restClient = restClient();
			final ObjectMapper objectMapper = objectMapper();
			final ResultDisplayMode displayMode = displayMode(this.displayMode);
			final LocationFilter filter = filter();

			if (!filter.hasCriteria()) {
				System.err.println("At leaset one search criteria must be provided.");
				return 1;
			}

			final List<LocationRequest> records = listLocationRequests(restClient, objectMapper, filter);
			if (records.isEmpty()) {
				System.err.println("No location requests match the search criteria.");
				return 1;
			}

			final List<?> tableData = (displayMode == ResultDisplayMode.JSON ? records
					: records.stream().map(ListLocationRequestsCmd::tableDataRow).toList());
			TableUtils.renderTableData(tableDataColumns(), tableData, tableConfig(this, displayMode, prettyStyle()),
					objectMapper, TableUtils.TableDataJsonPrettyPrinter.INSTANCE, System.out);
			return 0;
		} catch (Exception e) {
			System.err.println("Error listing location requests: %s".formatted(e.getMessage()));
		}
		return 1;
	}

	private LocationFilter filter() {
		final var filter = new LocationFilter();
		if (statuses != null) {
			filter.setLocationRequestStatuses(List.of(statuses));
		}
		return filter;
	}

	/**
	 * List location requests.
	 * 
	 * @param restClient   the REST client
	 * @param objectMapper the mapper to use
	 * @param filter       the search criteria
	 * @return the requests
	 */
	public static List<LocationRequest> listLocationRequests(final RestClient restClient,
			final ObjectMapper objectMapper, final LocationFilter filter) {
		// @formatter:off
		final JsonNode response = checkSuccess(restClient.get()
				.uri(b -> {
					b.path("/solaruser/api/v1/sec/location/meta/request");
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
			LocationRequest[] result = objectMapper.treeToValue(response.path("data").path("results"),
					LocationRequest[].class);
			return (result != null ? List.of(result) : List.of());
		} catch (JsonProcessingException | IllegalArgumentException e) {
			throw new IllegalStateException("Error parsing location request list response: " + e.getMessage(), e);
		}
	}

	/**
	 * Get location tabular structure columns.
	 * 
	 * @return the columns
	 */
	public static Column[] tableDataColumns() {
		// @formatter:off
		return new Column[] {
				new Column().header("ID").dataAlign(RIGHT),
				new Column().header("Created").dataAlign(LEFT),
				new Column().header("Modified").dataAlign(LEFT),
				new Column().header("Status").dataAlign(LEFT),
				new Column().header("Location ID").dataAlign(LEFT),
				new Column().header("Source ID").dataAlign(LEFT),
				new Column().header("Features").dataAlign(LEFT),
				new Column().header("Info").dataAlign(LEFT),
				new Column().header("Message").dataAlign(LEFT),
			};
		// @formatter:on
	}

	/**
	 * Convert a location request into a tabular structure.
	 * 
	 * @param request the request to convert
	 * @return the tabular data
	 */
	public static Object[] tableDataRow(LocationRequest request) {
		// @formatter:off
		return new Object[] {
				request.id(),
				request.created(),
				request.modified(),
				request.status(),
				request.locationId(),
				getMapString("sourceId", request.data()),
				mapPropertyStringList("features", request.data()),
				TableUtils.basicTable(mapPropertyStringMap("location", request.data()), null, null, false),
				request.message(),
			};
		// @formatter:on
	}

}
