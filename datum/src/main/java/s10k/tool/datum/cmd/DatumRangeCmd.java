package s10k.tool.datum.cmd;

import static com.github.freva.asciitable.HorizontalAlign.LEFT;
import static com.github.freva.asciitable.HorizontalAlign.RIGHT;
import static s10k.tool.common.util.RestUtils.populateQueryParameters;
import static s10k.tool.common.util.TableUtils.tableConfig;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.Callable;

import org.jspecify.annotations.Nullable;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.freva.asciitable.Column;

import net.solarnetwork.domain.datum.ObjectDatumKind;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.DateRangeInfo;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.util.DateUtils;
import s10k.tool.common.util.RestUtils;
import s10k.tool.common.util.TableUtils;
import s10k.tool.datum.domain.DatumFilter;

/**
 * Discover the time range of a datum stream.
 */
@Component
@Command(name = "date-range", aliases = "range", sortSynopsis = false, showDefaultValues = true)
public class DatumRangeCmd extends BaseSubCmd<DatumCmd> implements Callable<Integer> {

	// @formatter:off
	@ArgGroup(exclusive = true, multiplicity = "1")
	@SuppressWarnings("NullAway.Init")
	NodeOrLocationId nodeOrLocationId;

	@Option(names = { "-source", "--source-id" },
			description = "a source ID to return information for, otherwise all sources are considered")
	@Nullable String sourceId;
	
	@Option(names = { "-min", "--min-date" },
			description = "a minimum datum date to limit the range to")
	@Nullable LocalDateTime minDate;

	@Option(names = { "-max", "--max-date" },
			description = "a maximum datum date (exclusive) to limit the range to")
	@Nullable LocalDateTime maxDate;
	
	@Option(names = {"-local", "--local-dates"},
			description = "treat the min/max dates as 'node local' dates, instead of UTC (or local time zone when -tz used)")
	boolean useLocalDates;

	@Option(names = { "-tz", "--time-zone" },
			description = "a time zone to interpret the min and max dates as, instead of the local time zone")
	@Nullable ZoneId zone;
	
	@Option(names = { "-mode", "--display-mode" },
			description = "how to display the data")
	@Nullable ResultDisplayMode displayMode;
	// @formatter:on

	/**
	 * Grouping of node/location ID, where only one or the other should be
	 * specified.
	 */
	static class NodeOrLocationId {

		// @formatter:off
    	@Option(names = { "-node", "--node-id" },
    			description = "a node ID to return information for")
    	@Nullable Long nodeId;

    	@Option(names = { "-loc", "--location-id" },
    			description = "a location ID to return information for")
    	@Nullable Long locationId;
    	// @formatter:on

		/**
		 * Test if location IDs are provided (otherwise node IDs are).
		 * 
		 * @return {@code true} if location ID is configured, {@code false} if node ID
		 *         is
		 */
		boolean isLocation() {
			return locationId != null;
		}

	}

	/**
	 * Constructor.
	 * 
	 * @param reqFactory   the HTTP request factory to use
	 * @param objectMapper the mapper to use
	 */
	public DatumRangeCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		try {
			final RestClient restClient = restClient();
			final ObjectMapper objectMapper = objectMapper();
			final ResultDisplayMode displayMode = displayMode(this.displayMode);
			final DatumFilter filter = datumFilter();

			final DateRangeInfo result = viewReportableInterval(restClient, objectMapper, filter);

			List<?> tableData = (displayMode == ResultDisplayMode.JSON ? List.of(result)
					: List.of((Object) tableDataRow(result)));
			TableUtils.renderTableData(tableDataColumns(), tableData,
					tableConfig(this, displayMode, zone).asJsonSingleton(), objectMapper,
					TableUtils.TableDataJsonPrettyPrinter.INSTANCE, System.out);
			return 0;
		} catch (Exception e) {
			System.err.println("Error listing datum: %s".formatted(e.getMessage()));
		}
		return 1;
	}

	private DatumFilter datumFilter() {
		final DatumFilter filter = new DatumFilter();
		if (nodeOrLocationId.isLocation()) {
			filter.setObjectKind(ObjectDatumKind.Location);
			filter.setObjectIds(List.of(nodeOrLocationId.locationId));
		} else {
			filter.setObjectKind(ObjectDatumKind.Node);
			filter.setObjectIds(List.of(nodeOrLocationId.nodeId));
			filter.setIncludeStreamAliases(includeStreamAliases());
		}
		if (sourceId != null) {
			filter.setSourceIds(List.of(sourceId));
		}
		if (minDate != null) {
			if (useLocalDates) {
				filter.setLocalStartDate(minDate);
			} else {
				filter.setStartDate(DateUtils.zonedDate(minDate, zone));
			}
		}
		if (maxDate != null) {
			if (useLocalDates) {
				filter.setLocalEndDate(maxDate);
			} else {
				filter.setEndDate(DateUtils.zonedDate(maxDate, zone));
			}
		}
		return filter;
	}

	/**
	 * View the reportable interval for a node or location.
	 * 
	 * <p>
	 * A single node or location ID is required. An optional source ID can be
	 * provided.
	 * </p>
	 * 
	 * @param restClient   the REST client to use
	 * @param objectMapper the mapper to use
	 * @param filter       the search criteria
	 * @return the results, or {@code null} if not available
	 * @throws RestClientException if the request fails
	 */
	public static DateRangeInfo viewReportableInterval(RestClient restClient, ObjectMapper objectMapper,
			DatumFilter filter) {
		// @formatter:off
		final JsonNode response = RestUtils.checkSuccess(restClient.get()
			.uri(b -> {
				b.path(filter.isLocationQuery() 
						? "/solarquery/api/v1/sec/location/range/interval"
						: "/solarquery/api/v1/sec/range/interval");
				populateQueryParameters(b, () -> {
					MultiValueMap<String, Object> params = filter.toRequestMap();
					
					// rename node/location/sourceIds to singular form
					List<Object> ids = params.remove("nodeIds");
					if (ids != null ) {
						params.put("nodeId", ids);
					}
					ids = params.remove("locationIds");
					if (ids != null ) {
						params.put("locationId", ids);
					}
					ids = params.remove("sourceIds");
					if (ids != null ) {
						params.put("sourceId", ids);
					}
					
					return params;
				});
				return b.build();
			})
			.accept(MediaType.APPLICATION_JSON)
			.retrieve()
			.body(JsonNode.class)
			);
		// @formatter:on

		try {
			return objectMapper.treeToValue(response.path("data"), DateRangeInfo.class);
		} catch (JsonProcessingException | IllegalArgumentException e) {
			throw new IllegalStateException("Error parsing reportable interval response: " + e.getMessage(), e);
		}
	}

	/**
	 * Get date range info tabular structure columns.
	 * 
	 * @return the columns
	 * @see #tableDataRow(DateRangeInfo)
	 */
	public static Column[] tableDataColumns() {
		// @formatter:off
		return new Column[] {
				new Column().header("Start Date").dataAlign(LEFT),
				new Column().header("End Date").dataAlign(LEFT),
				new Column().header("Time Zone").dataAlign(LEFT),
				new Column().header("Years").dataAlign(RIGHT),
				new Column().header("Months").dataAlign(RIGHT),
				new Column().header("Days").dataAlign(RIGHT),
			};
		// @formatter:on
	}

	/**
	 * Convert a date range info into a tabular structure.
	 * 
	 * @param info the configuration to convert
	 * @return the data row
	 * @see #tableDataColumns()
	 */
	public static Object[] tableDataRow(DateRangeInfo info) {
		// @formatter:off
		return new Object[] {
				info.startDate(),
				info.endDate(),
				info.timeZone(),
				info.yearCount(),
				info.monthCount(),
				info.dayCount(),
			};
		// @formatter:on
	}

}
