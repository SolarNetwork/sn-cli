package s10k.tool.user.events.cmd;

import static com.github.freva.asciitable.HorizontalAlign.LEFT;
import static s10k.tool.common.util.RestUtils.checkSuccess;
import static s10k.tool.common.util.TableUtils.tableConfig;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SequencedSet;
import java.util.concurrent.Callable;

import org.jspecify.annotations.Nullable;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionException;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.HorizontalAlign;

import net.solarnetwork.util.CollectionUtils;
import net.solarnetwork.util.StringUtils;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.util.DateUtils;
import s10k.tool.common.util.ExpressionUtils;
import s10k.tool.common.util.RestUtils;
import s10k.tool.common.util.TableUtils;
import s10k.tool.user.events.domain.UserEvent;
import s10k.tool.user.events.domain.UserEventsFilter;

/**
 * List user events matching search criteria.
 */
@Command(name = "list", sortSynopsis = false, showDefaultValues = true, descriptionHeading = "%n", optionListHeading = "%n", description = {
		"List user events matching search criteria.%n" })
public class ListUserEventsCmd extends BaseSubCmd<BaseUserEventsCmd> implements Callable<Integer> {

	// @formatter:off
	@Option(names = { "-E", "--show-event-id" },
			description = "show event IDs in the tabular output modes")
	boolean includeEventId;

	@Option(names = { "-F", "--search-filter" },
			description = "an event data search filter to match")
	@Nullable String searchFilter;

	@Option(names = { "-t", "--tag" },
			description = "a tag to match",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "tag")
	String @Nullable [] tags;
	
	@Option(names = { "-min", "--min-date" },
			description = "a minimum datum date to match",
			required = true)
	@Nullable LocalDateTime minDate;

	@Option(names = { "-max", "--max-date" },
			description = "a maximum datum date (exclusive) to match",
			required = true)
	@Nullable LocalDateTime maxDate;

	@Option(names = { "-tz", "--time-zone" },
			description = "a time zone to interpret the min and max dates as, instead of the local time zone")
	@Nullable ZoneId zone;
	
	@Option(names = {"-M", "--max"},
			description = "return at most this many results", paramLabel = "max")
	int maxResults;

	@Option(names = {"-O", "--offset"},
			description = "start returning results from this offset, 0 being the first result")
	long resultOffset;

	@Option(names = { "-c", "--column" },
			description = "a name:path reference to extract from the event data as a column in tabular output mode",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "columReference")
	String @Nullable [] columnReferences;
	
	@Option(names = { "-x", "--expression" },
			description = "a name:expression reference to extract from the event data as a column in tabular output mode",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "columExpression")
	String @Nullable [] columExpressions;
	
	@Option(names = { "-q", "--quiet" },
			description = "suppress expression evaluation errors")
	boolean quiet;

	@Option(names = { "-D", "--show-data" },
			description = "show event data in the tabular output modes when --column options are given")
	boolean includeData;

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
	public ListUserEventsCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		try {
			final RestClient restClient = restClient();
			final ObjectMapper objectMapper = objectMapper();
			final ResultDisplayMode displayMode = displayMode(this.displayMode);
			final UserEventsFilter filter = filter();
			final Map<String, String> columnMapping = columnMapping(columnReferences);
			final Map<String, Expression> expressions = columnExpressions(columExpressions);
			final SequencedSet<String> extraColumns = new LinkedHashSet<String>(
					columnMapping.size() + expressions.size());
			extraColumns.addAll(columnMapping.keySet());
			extraColumns.addAll(expressions.keySet());

			final List<UserEvent> tasks = listUserEvents(restClient, objectMapper, filter);

			final List<?> tableData = (displayMode == ResultDisplayMode.JSON ? tasks
					: tasks.stream()
							.map(c -> tableDataRow(c, includeEventId, columnMapping, expressions, quiet, includeData))
							.toList());
			TableUtils.renderTableData(tableDataColumns(includeEventId, extraColumns, includeData), tableData,
					tableConfig(this, displayMode), objectMapper, TableUtils.TableDataJsonPrettyPrinter.INSTANCE,
					System.out);
			return 0;
		} catch (Exception e) {
			System.err.println("Error listing user events: %s".formatted(e.getMessage()));
		}
		return 1;
	}

	private UserEventsFilter filter() {
		final UserEventsFilter filter = new UserEventsFilter();
		if (minDate != null) {
			filter.setStartDate(DateUtils.zonedDate(minDate, zone));
		}
		if (maxDate != null) {
			filter.setEndDate(DateUtils.zonedDate(maxDate, zone));
		}
		if (tags != null && tags.length > 0) {
			filter.setTags(List.of(tags));
		}
		if (searchFilter != null && !searchFilter.isEmpty()) {
			filter.setSearchFilter(searchFilter);
		}
		if (maxResults > 0) {
			filter.setMax(maxResults);
		}
		if (resultOffset > 0) {
			filter.setOffset(resultOffset);
		}
		return filter;

	}

	/**
	 * List user events.
	 * 
	 * @param restClient   the REST client
	 * @param objectMapper the object mapper
	 * @param filter       the criteria
	 * @return the list of matching tasks
	 * @throws IllegalStateException if an error occurs
	 */
	public static List<UserEvent> listUserEvents(RestClient restClient, ObjectMapper objectMapper,
			UserEventsFilter filter) {
		// @formatter:off
		final JsonNode response = checkSuccess(restClient.get()
				.uri(b -> {
					b.path("/solaruser/api/v1/sec/user/events");
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
			UserEvent[] result = objectMapper.treeToValue(response.path("data"), UserEvent[].class);
			return (result != null ? List.of(result) : List.of());
		} catch (JsonProcessingException | IllegalArgumentException e) {
			throw new IllegalStateException("Error parsing user events list response: " + e.getMessage(), e);
		}
	}

	private static Map<String, String> columnMapping(final String @Nullable [] columnReferences) {
		if (columnReferences == null || columnReferences.length < 1) {
			return Map.of();
		}
		var result = new LinkedHashMap<String, String>(columnReferences.length);
		for (String colRef : columnReferences) {
			Map<String, String> refMap = StringUtils.delimitedStringToMap(colRef, ",", ":");
			if (refMap != null) {
				result.putAll(refMap);
			}
		}
		return result;
	}

	private static Map<String, Expression> columnExpressions(final String @Nullable [] columnExpressions) {
		final Map<String, String> mappings = columnMapping(columnExpressions);
		if (mappings.isEmpty()) {
			return Map.of();
		}
		var result = new LinkedHashMap<String, Expression>(mappings.size());
		for (Entry<String, String> e : mappings.entrySet()) {
			result.put(e.getKey(), ExpressionUtils.spelExpression(e.getValue()));
		}
		return result;
	}

	/**
	 * Get datum import job tabular structure columns.
	 *
	 * @param includeEventId {@code true} to include the event ID column
	 * @param columnNames    optional column names to use as additional columns
	 * @param includeData    if {@code columnReferences} are provided, then
	 *                       {@code true} to still include the <b>Data</b> column
	 * @return the columns
	 * @see #tableDataRow(DatumImportTaskInfo)
	 */
	public static Column[] tableDataColumns(final boolean includeEventId,
			final @Nullable Collection<String> columnNames, boolean includeData) {
		List<Column> result = new ArrayList<>(5);
		if (includeEventId) {
			result.add(new Column().header("Event ID").dataAlign(LEFT));
		}
		result.add(new Column().header("Event Date").dataAlign(LEFT));
		result.add(new Column().header("Tags").dataAlign(LEFT));
		result.add(new Column().header("Message").dataAlign(LEFT));

		if (columnNames != null && !columnNames.isEmpty()) {
			for (String colName : columnNames) {
				HorizontalAlign align;
				if (colName.endsWith(">")) {
					colName = colName.substring(0, colName.length() - 1);
					align = HorizontalAlign.RIGHT;
				} else {
					align = HorizontalAlign.LEFT;
				}
				result.add(new Column().header(colName).dataAlign(align));
			}
		}
		if (includeData || columnNames == null || columnNames.isEmpty()) {
			result.add(new Column().header("Data").dataAlign(LEFT));
		}
		return result.toArray(Column[]::new);
	}

	/**
	 * Convert a user event into a tabular structure.
	 *
	 * @param info           the event to convert
	 * @param includeEventId {@code true} to include the event ID column
	 * @param columnMapping  optional column name to data references to extract as
	 *                       additional columns; a {@code SequencedMap} should be
	 *                       used for consistent ordering
	 * @param quiet          suppress expression evaluation errors
	 * @param includeData    if {@code columnReferences} are provided, then
	 *                       {@code true} to still include the <b>Data</b> column
	 * @return the tabular data row
	 * @see #tableDataColumns()
	 */
	public static Object[] tableDataRow(final UserEvent info, final boolean includeEventId,
			final @Nullable Map<String, String> columnMapping, Map<String, Expression> expressions, boolean quiet,
			boolean includeData) {
		List<Object> result = new ArrayList<>(5);
		if (includeEventId) {
			result.add(info.eventId());
		}
		result.add(info.eventDate());
		result.add(StringUtils.commaDelimitedStringFromCollection(info.tags()));
		result.add(info.message());
		if (columnMapping != null && !columnMapping.isEmpty()) {
			for (String path : columnMapping.values()) {
				result.add(CollectionUtils.valueAtPath(path, info.data()));
			}
		}
		if (expressions != null && !expressions.isEmpty()) {
			for (Expression expr : expressions.values()) {
				Object exprResult;
				try {
					exprResult = ExpressionUtils.evaluateExpression(expr, null, info, Object.class);
				} catch (ExpressionException e) {
					exprResult = (quiet ? null : e.getMessage());
				}
				result.add(exprResult);
			}
		}
		if (includeData || ((columnMapping == null || columnMapping.isEmpty())
				&& (expressions == null || expressions.isEmpty()))) {
			result.add(TableUtils.basicTable(info.data(), null, null, false));
		}
		return result.toArray(Object[]::new);
	}

}
