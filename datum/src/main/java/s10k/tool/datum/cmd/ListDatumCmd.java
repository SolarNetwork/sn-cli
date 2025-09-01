package s10k.tool.datum.cmd;

import static com.github.freva.asciitable.HorizontalAlign.LEFT;
import static com.github.freva.asciitable.HorizontalAlign.RIGHT;
import static net.solarnetwork.util.NumberUtils.bigDecimalForNumber;
import static net.solarnetwork.util.NumberUtils.narrow;
import static net.solarnetwork.util.NumberUtils.round;
import static s10k.tool.common.util.RestUtils.cborToJson;

import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.SequencedSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;

import net.solarnetwork.domain.datum.Aggregation;
import net.solarnetwork.domain.datum.DatumProperties;
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

	private static final Set<DatumSamplesType> PROP_TYPES = EnumSet.of(DatumSamplesType.Instantaneous,
			DatumSamplesType.Accumulating, DatumSamplesType.Status);

	@Override
	public Integer call() throws Exception {
		final DatumFilter filter = datumFilter();

		final RestClient restClient = restClient();

		try {
			if (displayMode == ResultDisplayMode.PRETTY) {
				ObjectDatumStreamDataSet<StreamDatum> result = listDatum(restClient, filter);
				SequencedSet<String> propertyColumns = new LinkedHashSet<String>();
				if (propertyNames != null && propertyNames.length > 0) {
					propertyColumns.addAll(Arrays.asList(propertyNames));
				} else {
					// generate stable list from all stream properties
					for (UUID streamId : result.metadataStreamIds()) {
						ObjectDatumStreamMetadata meta = result.metadataForStreamId(streamId);
						propertyColumns.addAll(Arrays.asList(meta.getPropertyNames()));
					}
				}
				List<Column> columns = new ArrayList<>();
				columns.add(new Column().header("Timestmp").dataAlign(LEFT));
				columns.add(new Column().header("Stream ID").dataAlign(LEFT));
				columns.add(new Column().header("Object ID").dataAlign(LEFT));
				columns.add(new Column().header("Source ID").dataAlign(LEFT));
				final int rowSize = (propertyColumns.size() + 4);
				List<Object[]> data = new ArrayList<>();
				for (StreamDatum d : result) {
					ObjectDatumStreamMetadata meta = result.metadataForStreamId(d.getStreamId());
					Object[] row = new Object[rowSize];
					row[0] = d.getTimestamp();
					row[1] = d.getStreamId();
					row[2] = meta.getObjectId();
					row[3] = meta.getSourceId();
					int idx = 4;
					for (String propName : propertyColumns) {
						DatumProperties props = d.getProperties();
						for (DatumSamplesType type : PROP_TYPES) {
							int propIdx = meta.propertyIndex(type, propName);
							if (propIdx >= 0) {
								Object propVal = switch (type) {
								case Instantaneous -> props.instantaneousValue(propIdx);
								case Accumulating -> props.accumulatingValue(propIdx);
								case Status -> props.statusValue(propIdx);
								default -> null;
								};
								if (propVal instanceof Number n) {
									propVal = bigDecimalForNumber(narrow(round(n, 3), 2)).toPlainString();
								}
								if (data.size() == 0) {
									// populate Column
									columns.add(new Column().header(propName)
											.dataAlign(type == DatumSamplesType.Status ? LEFT : RIGHT));
								}
								row[idx++] = propVal;
								break;
							}
						}
					}
					data.add(row);
				}
				// @formatter:off
				AsciiTable.builder()
					.data(columns.toArray(Column[]::new), data.toArray(Object[][]::new))
					.writeTo(System.out)
					;
				// @formatter:on
				System.out.println();
			} else {
				listDatumDirect(restClient, objectMapper, filter, displayMode);
				System.out.println();
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
			filter.setStreamIds(Arrays.asList(streamIds));
		}
		if (nodeOrLocationIds != null) {
			if (nodeOrLocationIds.isLocation()) {
				filter.setObjectKind(ObjectDatumKind.Location);
				filter.setObjectIds(Arrays.asList(nodeOrLocationIds.locationIds));
			} else {
				filter.setObjectKind(ObjectDatumKind.Node);
				filter.setObjectIds(Arrays.asList(nodeOrLocationIds.nodeIds));
			}
		}
		if (sourceIds != null && sourceIds.length > 0) {
			filter.setSourceIds(Arrays.asList(sourceIds));
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
		return filter;
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
				b.path("/solarquery/api/v1/sec/datum/stream/datum");
				MultiValueMap<String, Object> params = filter.toRequestMap();
				for ( Entry<String, List<Object>> e : params.entrySet() ) {
					b.queryParam(e.getKey(), e.getValue());
				}
				return b.build();
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
				b.path("/solarquery/api/v1/sec/datum/stream/datum");
				MultiValueMap<String, Object> params = filter.toRequestMap();
				for ( Entry<String, List<Object>> e : params.entrySet() ) {
					b.queryParam(e.getKey(), e.getValue());
				}
				return b.build();
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
						cborToJson(objectMapper, res.getBody(), System.out);
					}
				}
				return null;
			})
			;
		// @formatter:on
	}

}
