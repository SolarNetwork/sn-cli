package s10k.tool.datum.del.cmd;

import static com.github.freva.asciitable.HorizontalAlign.LEFT;
import static com.github.freva.asciitable.HorizontalAlign.RIGHT;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNullElse;
import static s10k.tool.common.util.RestUtils.checkSuccess;
import static s10k.tool.common.util.TableUtils.tableConfig;

import java.time.LocalDateTime;
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

import net.solarnetwork.domain.datum.Aggregation;
import net.solarnetwork.domain.datum.ObjectDatumKind;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.util.RestUtils;
import s10k.tool.common.util.TableUtils;
import s10k.tool.datum.del.domain.DatumDeleteTaskInfo;
import s10k.tool.datum.domain.DatumFilter;
import s10k.tool.datum.domain.DatumRecordCounts;

/**
 * View datum import job status.
 */
@Command(name = "range", sortSynopsis = false, showDefaultValues = true, descriptionHeading = "%n", optionListHeading = "%n", description = {
		"Delete datum for a time range.%n" })
public class DeleteDatumCmd extends BaseSubCmd<DatumDeleteGroup> implements Callable<Integer> {

	// @formatter:off
	@Option(names = { "-node", "--node-id" },
			description = "a node ID of datum to delete",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "nodeId")
	Long @Nullable [] nodeIds;

	@Option(names = { "-source", "--source-id" },
			description = "a source ID of datum to delete",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "sourceId")
	String @Nullable [] sourceIds;
	
	@Option(names = { "-min", "--min-date" },
			description = "a minimum datum date to delete",
			required = true)
	@SuppressWarnings("NullAway.Init")
	LocalDateTime minDate;

	@Option(names = { "-max", "--max-date" },
			description = "a maximum datum date (exclusive) to delete",
			required = true)
	@SuppressWarnings("NullAway.Init")
	LocalDateTime maxDate;

	@Option(names = {"-agg", "--aggregation"},
			description = "a maximum aggregation level to delete (inclusive); ignored if --dry-run given")
	@Nullable Aggregation aggregation;
	
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
	public DeleteDatumCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		try {
			final RestClient restClient = restClient();
			final ObjectMapper objectMapper = objectMapper();
			final ResultDisplayMode displayMode = displayMode(this.displayMode);
			final DatumFilter filter = datumFilter();

			if (isDryRun()) {
				DatumRecordCounts preview = previewDatumDeleteTask(restClient, objectMapper, filter);

				List<?> tableData = (displayMode == ResultDisplayMode.JSON ? List.of(preview)
						: List.of((Object) datumRecordCountsTableDataRow(preview)));
				TableUtils.renderTableData(datumRecordCountsTableDataColumns(), tableData,
						tableConfig(this, displayMode, prettyStyle()).asJsonSingleton(), objectMapper,
						TableUtils.TableDataJsonPrettyPrinter.INSTANCE, System.out);
			} else {
				DatumDeleteTaskInfo info = submitDeleteDatumTask(restClient, objectMapper, filter);

				List<?> tableData = (displayMode == ResultDisplayMode.JSON ? List.of(info)
						: List.of((Object) ViewDeleteJobCmd.tableDataRow(info)));
				TableUtils.renderTableData(ViewDeleteJobCmd.tableDataColumns(), tableData,
						tableConfig(this, displayMode, prettyStyle()).asJsonSingleton(), objectMapper,
						TableUtils.TableDataJsonPrettyPrinter.INSTANCE, System.out);
			}
			return 0;
		} catch (Exception e) {
			System.err.println("Error deleting datum: %s".formatted(e.getMessage()));
		}
		return 1;
	}

	private DatumFilter datumFilter() {
		final DatumFilter filter = new DatumFilter();
		if (nodeIds != null && nodeIds.length > 0) {
			filter.setObjectKind(ObjectDatumKind.Node);
			filter.setObjectIds(asList(nodeIds));
		}
		if (sourceIds != null && sourceIds.length > 0) {
			filter.setSourceIds(asList(sourceIds));
		}
		if (minDate != null) {
			filter.setLocalStartDate(minDate);
		}
		if (maxDate != null) {
			filter.setLocalEndDate(maxDate);
		}
		filter.setAggregation(aggregation);
		return filter;
	}

	/**
	 * Preview a datum delete task.
	 * 
	 * @param restClient   the REST client
	 * @param objectMapper the object mapper
	 * @param filter       the delete criteria
	 * @return the expected delete information
	 * @throws IllegalStateException if an error occurs
	 */
	public static DatumRecordCounts previewDatumDeleteTask(RestClient restClient, ObjectMapper objectMapper,
			DatumFilter filter) {
		// @formatter:off
		final JsonNode response = checkSuccess(restClient.post()
			.uri(b -> {
				b.path("/solaruser/api/v1/sec/user/expire/datum-delete");
				RestUtils.populateQueryParameters(b, filter::toRequestMap);
				return b.build();
			})
			.accept(MediaType.APPLICATION_JSON)
			.retrieve()
			.body(JsonNode.class)
			);		
		// @formatter:on

		try {
			return objectMapper.treeToValue(response.path("data"), DatumRecordCounts.class);
		} catch (JsonProcessingException | IllegalArgumentException e) {
			throw new IllegalStateException("Error parsing datum delete preview response: " + e.getMessage(), e);
		}
	}

	/**
	 * Get datum record counts tabular structure columns.
	 * 
	 * @return the columns
	 * @see #datumRecordCountsTableDataRow(DatumRecordCounts)
	 */
	public static Column[] datumRecordCountsTableDataColumns() {
		// @formatter:off
		return new Column[] {
				new Column().header("Date").dataAlign(LEFT),
				new Column().header("Total").dataAlign(RIGHT),
				new Column().header("Raw Datum").dataAlign(RIGHT),
				new Column().header("Hourly Datum").dataAlign(RIGHT),
				new Column().header("Daily Datum").dataAlign(RIGHT),
				new Column().header("Monthly Datum").dataAlign(RIGHT),
			};
		// @formatter:on
	}

	/**
	 * Convert datum record counts into a tabular structure.
	 * 
	 * @param info the configuration to convert
	 * @return the row data
	 * @see #datumRecordCountsTableDataColumns()
	 */
	public static Object[] datumRecordCountsTableDataRow(DatumRecordCounts info) {
		// @formatter:off
		return new Object[] {
				info.date(),
				info.datumTotalCount(),
				requireNonNullElse(info.datumCount(), 0L),
				requireNonNullElse(info.datumHourlyCount(), 0L),
				requireNonNullElse(info.datumDailyCount(), 0L),
				requireNonNullElse(info.datumMonthlyCount(), 0L),
			};
		// @formatter:on
	}

	/**
	 * Submit a datum delete task.
	 * 
	 * @param restClient   the REST client
	 * @param objectMapper the object mapper
	 * @param filter       the delete criteria
	 * @return the expected delete information
	 * @throws IllegalStateException if an error occurs
	 */
	public static DatumDeleteTaskInfo submitDeleteDatumTask(RestClient restClient, ObjectMapper objectMapper,
			DatumFilter filter) {
		// @formatter:off
		final JsonNode response = checkSuccess(restClient.post()
			.uri(b -> {
				b.path("/solaruser/api/v1/sec/user/expire/datum-delete/confirm");
				RestUtils.populateQueryParameters(b, filter::toRequestMap);
				return b.build();
			})
			.accept(MediaType.APPLICATION_JSON)
			.retrieve()
			.body(JsonNode.class)
			);		
		// @formatter:on

		try {
			return objectMapper.treeToValue(response.path("data"), DatumDeleteTaskInfo.class).normalized();
		} catch (JsonProcessingException | IllegalArgumentException e) {
			throw new IllegalStateException("Error parsing datum delete response: " + e.getMessage(), e);
		}
	}

}
