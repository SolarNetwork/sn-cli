package s10k.tool.datum.stale.cmd;

import static com.github.freva.asciitable.HorizontalAlign.LEFT;
import static com.github.freva.asciitable.HorizontalAlign.RIGHT;
import static java.util.Arrays.asList;
import static s10k.tool.common.util.RestUtils.checkSuccess;
import static s10k.tool.common.util.TableUtils.tableConfig;

import java.time.LocalDateTime;
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

import net.solarnetwork.domain.SimpleSortDescriptor;
import net.solarnetwork.domain.datum.Aggregation;
import net.solarnetwork.domain.datum.ObjectDatumKind;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.util.DateUtils;
import s10k.tool.common.util.RestUtils;
import s10k.tool.common.util.TableUtils;
import s10k.tool.datum.cmd.DatumCmd;
import s10k.tool.datum.domain.DatumFilter;
import s10k.tool.datum.stale.domain.StaleNodeDatumAggregate;

/**
 * Query for datum.
 */
@Command(name = "list", sortSynopsis = false, showDefaultValues = true, descriptionHeading = "%n", optionListHeading = "%n", description = {
		"List stale datum aggregate records matching search criteria.%n",

		"SolarNetwork continuously processes stale datum aggregate records, and the",
		"records are removed when the associated aggregate period is no longer stale.%n" })
public class ListStaleAggregatesCmd extends BaseSubCmd<DatumCmd> implements Callable<Integer> {

	// @formatter:off
	@Option(names = { "-node", "--node-id" },
			description = "a node ID to return datum for",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "nodeId")
	@SuppressWarnings("NullAway.Init")
	Long[] nodeIds;

	@Option(names = { "-source", "--source-id" },
			description = "a source ID to return datum for",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "sourceId")
	String @Nullable [] sourceIds;
	
	@Option(names = { "-ident", "--stream-ident" },
			description = "an object:source stream identifier to return records for; if provided then -node and -source are ignored",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "identifier")
	String @Nullable [] streamIdentifiers;
	
	@Option(names = { "-min", "--min-date" },
			description = "a minimum datum date to match")
	@Nullable LocalDateTime minDate;

	@Option(names = { "-max", "--max-date" },
			description = "a maximum datum date (exclusive) to match")
	@Nullable LocalDateTime maxDate;
	
	@Option(names = { "-tz", "--time-zone" },
			description = "a time zone to interpret the min and max dates as, instead of the local time zone")
	@Nullable ZoneId zone;
	
	@Option(names = {"-agg", "--aggregation"},
			description = "an aggregation level to return")
	@Nullable Aggregation aggregation;
	
	@Option(names = {"-M", "--max"},
			description = "return at most this many results", paramLabel = "max")
	int maxResults;

	@Option(names = {"-O", "--offset"},
			description = "start returning results from this offset, 0 being the first result")
	long resultOffset;

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
	public ListStaleAggregatesCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		try {
			final RestClient restClient = restClient();
			final ObjectMapper objectMapper = objectMapper();
			final ResultDisplayMode displayMode = displayMode(this.displayMode);
			final DatumFilter filter = datumFilter();

			if (!filter.hasNodeIds()) {
				System.err.println("At least one node ID must be provided.");
				return 1;
			}

			final List<StaleNodeDatumAggregate> results = listStaleNodeDatumAggregates(restClient, objectMapper,
					filter);

			if (results.isEmpty()) {
				System.err.println("No stale records match the given criteria.");
			}

			final List<?> tableData = (displayMode == ResultDisplayMode.JSON ? results
					: results.stream().map(c -> tableDataRow(c)).toList());
			TableUtils.renderTableData(tableDataColumns(), tableData, tableConfig(this, displayMode, zone),
					objectMapper, TableUtils.TableDataJsonPrettyPrinter.INSTANCE, System.out);
			return 0;
		} catch (Exception e) {
			System.err.println("Error listing datum aggregate records: %s".formatted(e.getMessage()));
		}
		return 1;
	}

	private DatumFilter datumFilter() {
		final DatumFilter filter = new DatumFilter();
		filter.setObjectKind(ObjectDatumKind.Node);
		if (streamIdentifiers != null && streamIdentifiers.length > 0) {
			filter.populateIdsFromStreamIdentifiers(asList(streamIdentifiers));
		} else {
			if (nodeIds != null) {
				filter.setObjectIds(asList(nodeIds));
			}
			if (sourceIds != null && sourceIds.length > 0) {
				filter.setSourceIds(asList(sourceIds));
			}
		}
		if (minDate != null) {
			filter.setStartDate(DateUtils.zonedDate(minDate, zone));
		}
		if (maxDate != null) {
			filter.setEndDate(DateUtils.zonedDate(maxDate, zone));
		}
		filter.setWithoutTotalResultsCount(true);
		filter.setAggregation(aggregation);
		if (maxResults > 0) {
			filter.setMax(maxResults);
		}
		if (resultOffset > 0) {
			filter.setOffset(resultOffset);
		}

		// force sort order
		filter.setSorts(SimpleSortDescriptor.sorts("kind", "stream", "time"));

		return filter;
	}

	/**
	 * List stale node datum aggregates records.
	 * 
	 * @param restClient   the REST client
	 * @param objectMapper the object mapper
	 * @param filter       the criteria
	 * @return the list of matching records
	 * @throws IllegalStateException if an error occurs
	 */
	public static List<StaleNodeDatumAggregate> listStaleNodeDatumAggregates(RestClient restClient,
			ObjectMapper objectMapper, DatumFilter filter) {
		// @formatter:off
		final JsonNode response = checkSuccess(restClient.get()
				.uri(b -> {
					b.path("/solaruser/api/v1/sec/datum/maint/agg/stale");
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
			StaleNodeDatumAggregate[] result = objectMapper.treeToValue(response.path("data").path("results"),
					StaleNodeDatumAggregate[].class);
			return (result != null ? List.of(result) : List.of());
		} catch (JsonProcessingException | IllegalArgumentException e) {
			throw new IllegalStateException("Error parsing stale node datum aggregate list response: " + e.getMessage(),
					e);
		}
	}

	/**
	 * Get datum import job tabular structure columns.
	 *
	 * @return the columns
	 * @see #tableDataRow(StaleNodeDatumAggregate)
	 */
	public static Column[] tableDataColumns() {
		// @formatter:off
		return new Column[] {
				new Column().header("Kind").dataAlign(LEFT),
				new Column().header("Node ID").dataAlign(RIGHT),				
				new Column().header("Source ID").dataAlign(LEFT),
				new Column().header("Period Start").dataAlign(LEFT),
			};
		// @formatter:on
	}

	/**
	 * Convert a stale node datum aggregate into a tabular structure.
	 *
	 * @param info the configuration to convert
	 * @return the row data
	 * @see #tableDataColumns()
	 */
	public static Object[] tableDataRow(StaleNodeDatumAggregate info) {
		// @formatter:off
		return new Object[] {
				info.kind(),
				info.nodeId(),
				info.sourceId(),
				info.startDate(),
			};
		// @formatter:on
	}
}
