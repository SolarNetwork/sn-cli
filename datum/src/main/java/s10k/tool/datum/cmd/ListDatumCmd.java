package s10k.tool.datum.cmd;

import static com.github.freva.asciitable.HorizontalAlign.LEFT;
import static com.github.freva.asciitable.HorizontalAlign.RIGHT;
import static java.util.Arrays.asList;
import static net.solarnetwork.domain.datum.DatumSamplesType.Accumulating;
import static net.solarnetwork.util.NumberUtils.bigDecimalForNumber;
import static net.solarnetwork.util.NumberUtils.narrow;
import static net.solarnetwork.util.NumberUtils.round;
import static org.springframework.util.StreamUtils.nonClosing;
import static s10k.tool.common.util.RestUtils.cborToJson;
import static s10k.tool.common.util.RestUtils.populateQueryParameters;

import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.SequencedSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;

import net.solarnetwork.domain.datum.AggregateStreamDatum;
import net.solarnetwork.domain.datum.Aggregation;
import net.solarnetwork.domain.datum.DatumProperties;
import net.solarnetwork.domain.datum.DatumPropertiesStatistics;
import net.solarnetwork.domain.datum.DatumReadingType;
import net.solarnetwork.domain.datum.DatumSamplesType;
import net.solarnetwork.domain.datum.ObjectDatumKind;
import net.solarnetwork.domain.datum.ObjectDatumStreamDataSet;
import net.solarnetwork.domain.datum.ObjectDatumStreamMetadata;
import net.solarnetwork.domain.datum.StreamDatum;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.util.DateUtils;
import s10k.tool.common.util.LocalDateTimeConverter;
import s10k.tool.common.util.SystemUtils;
import s10k.tool.datum.domain.DatumFilter;

/**
 * View stream information.
 */
@Component
@Command(name = "list")
public class ListDatumCmd extends BaseSubCmd<DatumCmd> implements Callable<Integer> {

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
	
	@Option(names = { "-min", "--min-date" },
			description = "a minimum datum date to match",
			converter = LocalDateTimeConverter.class)
	LocalDateTime minDate;

	@Option(names = { "-max", "--max-date" },
			description = "a maximum datum date (exclusive) to match",
			converter = LocalDateTimeConverter.class)
	LocalDateTime maxDate;
	
	@Option(names = {"-local", "--local-dates"},
			description = "treat the min/max dates as 'node local' dates, instead of UTC (or local time zone when -tz used)")
	boolean useLocalDates;

	@Option(names = {"--with-total-result-count"},
			description = "include a total result count in the results, if paged results are returned")
	boolean totalResultsCount;

	@Option(names = { "-tz", "--time-zone" },
			description = "a time zone to interpret the min and max dates as, instead of the local time zone")
	ZoneId zone;
	
	@Option(names = {"-recent", "--most-recent"},
			description = "show just the most recently available data, within min/max dates if specified")
	boolean mostRecent;

	@Option(names = {"-agg", "--aggregation"},
			description = "an aggregation level to return")
	Aggregation aggregation;
	
	@Option(names = {"-pagg", "--partial-aggregation"},
			description = "a partial aggregation level to return")
	Aggregation partialAggregation;
	
	@Option(names = {"-read", "--reading"},
			description = "return a reading aggregation result instead of a listing result")
	DatumReadingType readingType;
	
	@Option(names = {"-tol", "--tolerance"},
			description = "a time tolerance to use with reading-style queries that support it")
	Period timeTolerance;

	@Option(names = { "-prop", "--property" },
			description = "show only this property name in the results (applies only to PRETTY display mode)",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "propName")
	String[] propertyNames;
	
	@Option(names = { "-mode", "--display-mode" },
			description = "how to display the CSV data",
			defaultValue = "PRETTY")
	ResultDisplayMode displayMode = ResultDisplayMode.PRETTY;

	@Option(names = {"-S", "--show-stream-ids"},
			description = "show stream IDs in PRETTY results")
	boolean showStreamIds;
	
	@Option(names = {"-M", "--max"},
			description = "return at most this many results", paramLabel = "max")
	int maxResults;

	@Option(names = {"-O", "--offset"},
			description = "start returning results from this offset, 0 being the first result")
	long resultOffset;
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
	public ListDatumCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		final DatumFilter filter = datumFilter();

		final RestClient restClient = restClient();

		try {
			if (displayMode == ResultDisplayMode.PRETTY) {
				ObjectDatumStreamDataSet<StreamDatum> result = listDatum(restClient, filter);
				SequencedSet<String> propertyColumns = prettyProperties(filter, result);
				List<Column> columns = prettyColumns(filter);
				List<Object[]> data = prettyRows(filter, result, propertyColumns, columns);
				// @formatter:off
				AsciiTable.builder()
					.data(columns.toArray(Column[]::new), data.toArray(Object[][]::new))
					.writeTo(System.out)
					;
				// @formatter:on
				System.out.println();
			} else {
				listDatumDirect(restClient, objectMapper, filter, displayMode);
			}
			return 0;
		} catch (Exception e) {
			System.err.println("Error listing datum: %s".formatted(e.getMessage()));
		}
		return 1;
	}

	private DatumFilter datumFilter() {
		final DatumFilter filter = new DatumFilter();
		if (streamIds != null && streamIds.length > 0) {
			filter.setStreamIds(asList(streamIds));
		}
		if (nodeOrLocationIds != null) {
			if (nodeOrLocationIds.isLocation()) {
				filter.setObjectKind(ObjectDatumKind.Location);
				filter.setObjectIds(asList(nodeOrLocationIds.locationIds));
			} else {
				filter.setObjectKind(ObjectDatumKind.Node);
				filter.setObjectIds(asList(nodeOrLocationIds.nodeIds));
			}
		}
		if (sourceIds != null && sourceIds.length > 0) {
			filter.setSourceIds(asList(sourceIds));
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
		filter.setWithoutTotalResultsCount(!totalResultsCount);
		filter.setMostRecent(mostRecent);
		filter.setAggregation(aggregation);
		filter.setPartialAggregation(partialAggregation);
		filter.setReadingType(readingType);
		filter.setTimeTolerance(timeTolerance);
		if (maxResults > 0) {
			filter.setMax(maxResults);
		}
		if (resultOffset > 0) {
			filter.setOffset(resultOffset);
		}
		return filter;
	}

	private static final Set<DatumSamplesType> PROP_TYPES = EnumSet.of(DatumSamplesType.Instantaneous,
			DatumSamplesType.Accumulating, DatumSamplesType.Status);

	/**
	 * Generate a set of datum property names to render in tabular form for pretty
	 * output.
	 * 
	 * @param filter the query filter
	 * @param result the results
	 * @return the set of property names
	 */
	private SequencedSet<String> prettyProperties(DatumFilter filter, ObjectDatumStreamDataSet<StreamDatum> result) {
		SequencedSet<String> properties = new LinkedHashSet<String>();
		if (propertyNames != null && propertyNames.length > 0) {
			properties.addAll(asList(propertyNames));
		} else {
			// generate stable list from all stream properties
			for (UUID streamId : result.metadataStreamIds()) {
				ObjectDatumStreamMetadata meta = result.metadataForStreamId(streamId);
				properties.addAll(asList(filter.isReadingRecordStyle() ? meta.propertyNamesForType(Accumulating)
						: meta.getPropertyNames()));
			}
		}
		return properties;
	}

	/**
	 * Generate the initial set of tabular {@code Column} instances for pretty
	 * output.
	 * 
	 * @param filter the query filter
	 * @return the initial list of column definitions, to be updated with values for
	 *         individual property columns later
	 */
	private List<Column> prettyColumns(DatumFilter filter) {
		List<Column> columns = new ArrayList<>();
		columns.add(new Column().header(filter.isAggregateStyle() ? "Timestamp Start" : "Timestamp").dataAlign(LEFT));
		if (filter.isReadingRecordStyle()) {
			columns.add(new Column().header("Timestamp End").dataAlign(LEFT));
		}
		if (showStreamIds) {
			columns.add(new Column().header("Stream ID").dataAlign(LEFT));
		}
		columns.add(new Column().header("Object ID").dataAlign(LEFT));
		columns.add(new Column().header("Source ID").dataAlign(LEFT));
		return columns;
	}

	/**
	 * Generate tabular data rows for pretty output.
	 * 
	 * @param filter    the query filter
	 * @param result    the results
	 * @param propNames the set of property names to include in the results
	 * @param columns   the set of column definitions, to add {@code Column}
	 *                  instances to for each property name in {@code propertyNames}
	 * @return the data rows
	 */
	private List<Object[]> prettyRows(DatumFilter filter, ObjectDatumStreamDataSet<StreamDatum> result,
			SequencedSet<String> propNames, List<Column> columns) {
		final Map<String, DatumSamplesType> propTypes = new HashMap<>(propNames.size());
		final int rowSize = (propNames.size() + columns.size());
		List<Object[]> data = new ArrayList<>();
		for (StreamDatum d : result) {
			final AggregateStreamDatum agg = (d instanceof AggregateStreamDatum a ? a : null);
			final ObjectDatumStreamMetadata meta = result.metadataForStreamId(d.getStreamId());
			final Object[] row = new Object[rowSize];
			int idx = 0;
			row[idx++] = d.getTimestamp();
			if (filter.isReadingRecordStyle()) {
				row[idx++] = (agg != null ? agg.getEndTimestamp() : null);
			}
			if (showStreamIds) {
				row[idx++] = d.getStreamId();
			}
			row[idx++] = meta.getObjectId();
			row[idx++] = meta.getSourceId();
			for (String propName : propNames) {
				final DatumSamplesType propType = propTypes.computeIfAbsent(propName, p -> {
					for (DatumSamplesType type : PROP_TYPES) {
						if (meta.propertyIndex(type, propName) >= 0) {
							return type;
						}
					}
					return null;
				});
				if (data.size() == 0) {
					// populate Column
					columns.add(new Column().header(propName)
							.dataAlign(propType == DatumSamplesType.Status ? LEFT : RIGHT));
				}
				if (propType == null) {
					continue;
				}
				DatumProperties props = d.getProperties();
				final int propIdx = meta.propertyIndex(propType, propName);
				if (propIdx < 0) {
					continue;
				}
				Object propVal = switch (propType) {
				case Instantaneous -> props.instantaneousValue(propIdx);
				case Accumulating -> {
					if (filter.isReadingAggregateStyle() && agg != null) {
						DatumPropertiesStatistics stats = agg.getStatistics();
						yield stats.getAccumulatingDifference(propIdx);
					} else {
						yield props.accumulatingValue(propIdx);
					}
				}
				case Status -> props.statusValue(propIdx);
				default -> null;
				};

				if (propVal instanceof Number n) {
					propVal = bigDecimalForNumber(narrow(round(n, 3), 2)).toPlainString();
				}
				row[idx++] = propVal;
			}
			data.add(row);
		}
		return data;
	}

	private static final ParameterizedTypeReference<ObjectDatumStreamDataSet<StreamDatum>> STREAM_DATUM_SET_TYPEREF = new ParameterizedTypeReference<ObjectDatumStreamDataSet<StreamDatum>>() {
	};

	/**
	 * Query for "raw" datum.
	 * 
	 * @param restClient the REST client to use
	 * @param filter     the search criteria
	 * @return the results, or {@code null} if not available
	 * @throws RestClientException if the request fails
	 */
	public static ObjectDatumStreamDataSet<StreamDatum> listDatum(RestClient restClient, DatumFilter filter) {
		// @formatter:off
		return restClient.get()
			.uri(b -> {
				b.path("/solarquery/api/v1/sec/datum/stream/{style}");
				populateQueryParameters(b, filter::toRequestMap);
				return b.build(filter.getReadingType() != null ? "reading" : "datum");
			})
			.accept(MediaType.APPLICATION_CBOR)
			.retrieve()
			.body(STREAM_DATUM_SET_TYPEREF)
			;
		// @formatter:on
	}

	/**
	 * Query for datum and directly dump the results to the system out.
	 * 
	 * @param restClient   the REST client to use
	 * @param objectMapper the object mapper to use
	 * @param filter       the search criteria
	 * @param displayMode  either CSV or JSON
	 * @throws RestClientException if the request fails
	 */
	public static void listDatumDirect(RestClient restClient, ObjectMapper objectMapper, DatumFilter filter,
			ResultDisplayMode displayMode) {
		// @formatter:off
		restClient.get()
			.uri(b -> {
				b.path("/solarquery/api/v1/sec/datum/stream/{style}");
				populateQueryParameters(b, filter::toRequestMap);
				return b.build(filter.getReadingType() != null ? "reading" : "datum");
			})
			.accept(displayMode == ResultDisplayMode.CSV
					? MediaType.valueOf("text/csv")
					: MediaType.APPLICATION_CBOR)
			.exchange((req, res) -> {
				if (res.getStatusCode().is2xxSuccessful()) {
					if (displayMode == ResultDisplayMode.CSV ) {
						StreamUtils.copy(res.getBody(), System.out);						
					} else {
						// convert CBOR to JSON
						cborToJson(objectMapper, res.getBody(), nonClosing(System.out));
						if (SystemUtils.systemConsoleIsTerminal()) {
							System.out.println();
						}
					}
				}
				return null;
			})
			;
		// @formatter:on
	}

}
