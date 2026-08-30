package s10k.tool.datum.stale.cmd;

import static java.util.Arrays.asList;
import static s10k.tool.common.util.RestUtils.checkSuccess;
import static s10k.tool.common.util.TableUtils.tableConfig;
import static s10k.tool.datum.stale.cmd.ListStaleAggregatesCmd.listStaleNodeDatumAggregates;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.Callable;

import org.jspecify.annotations.Nullable;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.solarnetwork.domain.datum.ObjectDatumKind;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.util.DateUtils;
import s10k.tool.common.util.RestUtils;
import s10k.tool.common.util.TableUtils;
import s10k.tool.datum.cmd.DatumGroup;
import s10k.tool.datum.domain.DatumFilter;
import s10k.tool.datum.stale.domain.StaleNodeDatumAggregate;

/**
 * Mark datum aggregates as "stale".
 */
@Command(name = "mark", sortSynopsis = false, showDefaultValues = true, descriptionHeading = "%n", optionListHeading = "%n", description = {
		"Mark aggregate records for time ranges of datum streams as @|bold stale|@.",
		"This will cause SolarNetwork to recalculate the stale aggregate records,",
		"including any enclosing higher-level aggregate records.%n" })
public class MarkAggregatesStaleCmd extends BaseSubCmd<DatumGroup> implements Callable<Integer> {

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
	 * Constructor.
	 * 
	 * @param reqFactory   the HTTP request factory to use
	 * @param objectMapper the mapper to use
	 */
	public MarkAggregatesStaleCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
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

			if (!filter.hasSourceIds()) {
				System.err.println("At least one source ID must be provided.");
				return 1;
			}

			if (!filter.hasSomeDateRange()) {
				System.err.println("A date range must be provided.");
				return 1;
			}

			markNodeDatumAggregatesStale(restClient, objectMapper, filter);

			// because we don't support local date ranges in the list command, just query
			// without a date range for the final results
			filter.clearLocalDates();
			List<StaleNodeDatumAggregate> results = listStaleNodeDatumAggregates(restClient, objectMapper, filter);

			if (results.isEmpty()) {
				System.err.println("No stale records were generated.");
			}

			final List<?> tableData = (displayMode == ResultDisplayMode.JSON ? results
					: results.stream().map(c -> ListStaleAggregatesCmd.tableDataRow(c)).toList());
			TableUtils.renderTableData(ListStaleAggregatesCmd.tableDataColumns(), tableData,
					tableConfig(this, displayMode, prettyStyle(), zone), objectMapper, TableUtils.TableDataJsonPrettyPrinter.INSTANCE,
					System.out);
			return 0;
		} catch (Exception e) {
			System.err.println("Error marking datum aggregate records stale: %s".formatted(e.getMessage()));
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
		filter.setWithoutTotalResultsCount(true);

		return filter;
	}

	/**
	 * Mark node datum aggregates records as stale.
	 * 
	 * @param restClient   the REST client
	 * @param objectMapper the object mapper
	 * @param filter       the criteria
	 * @throws IllegalStateException if an error occurs
	 */
	public static void markNodeDatumAggregatesStale(RestClient restClient, ObjectMapper objectMapper,
			DatumFilter filter) {
		// @formatter:off
		checkSuccess(restClient.post()
				.uri(b -> {
					b.path("/solaruser/api/v1/sec/datum/maint/agg/stale");
					RestUtils.populateQueryParameters(b, filter::toRequestMap);
					return b.build();
				})
			.accept(MediaType.APPLICATION_JSON)
			.retrieve()
			.body(JsonNode.class)
			);		
		// @formatter:on
	}

}
