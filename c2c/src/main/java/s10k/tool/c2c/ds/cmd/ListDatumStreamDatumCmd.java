package s10k.tool.c2c.ds.cmd;

import static com.github.freva.asciitable.HorizontalAlign.LEFT;
import static com.github.freva.asciitable.HorizontalAlign.RIGHT;
import static java.util.stream.StreamSupport.stream;
import static org.springframework.util.StreamUtils.nonClosing;
import static s10k.tool.common.util.RestUtils.checkSuccess;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.github.freva.asciitable.Column;

import net.solarnetwork.domain.datum.Datum;
import net.solarnetwork.domain.datum.DatumAuxiliaryRecord;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import s10k.tool.c2c.domain.BasicCloudDatumStreamQueryResult;
import s10k.tool.c2c.domain.CloudDatumStreamQueryResult;
import s10k.tool.c2c.domain.CloudIntegrationsFilter;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.util.DateUtils;
import s10k.tool.common.util.DatumUtils;
import s10k.tool.common.util.DatumUtils.DatumResultStructure;
import s10k.tool.common.util.TableUtils;

/**
 * Query for Cloud Datum Stream datum.
 */
@Component
@Command(name = "datum", sortSynopsis = false)
public class ListDatumStreamDatumCmd extends BaseSubCmd<DatumStreamsCmd> implements Callable<Integer> {

	// @formatter:off
	@Option(names = { "-stream", "--stream-id" },
			description = "the ID of the datum stream to show data values for",
			required =  true)
	Long datumStreamId;

	@ArgGroup(exclusive = false, multiplicity = "0..1")
	DateRange dateRange;
	
	@Option(names = { "-tz", "--time-zone" },
			description = "a time zone to interpret the min and max dates as, instead of the local time zone")
	ZoneId zone;
	
	@Option(names = { "-mode", "--display-mode" },
			description = "how to display the data",
			defaultValue = "PRETTY")
	ResultDisplayMode displayMode = ResultDisplayMode.PRETTY;
	// @formatter:on

	/**
	 * Grouping of min/max date range.
	 */
	static class DateRange {
		// @formatter:off
		@Option(names = { "-min", "--min-date" },
				description = "a minimum datum date to match",
				required = true)
		LocalDateTime minDate;

		@Option(names = { "-max", "--max-date" },
				description = "a maximum datum date (exclusive) to match",
				required = true)
		LocalDateTime maxDate;
    	// @formatter:on

	}

	/**
	 * Constructor.
	 * 
	 * @param reqFactory   the HTTP request factory to use
	 * @param objectMapper the mapper to use
	 */
	public ListDatumStreamDatumCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		final RestClient restClient = restClient();
		final CloudIntegrationsFilter filter = filter();

		try {
			CloudDatumStreamQueryResult datum = listCloudDatumStreamDatum(restClient, objectMapper, datumStreamId,
					filter);
			if (datum.isEmpty()) {
				System.err.println("No datum matched your criteria.");
				return 0;
			}

			if (displayMode == ResultDisplayMode.JSON) {
				objectMapper.writeValue(nonClosing(System.out), datum);
			} else {
				final DatumResultStructure structure = DatumUtils.resultStructure(datum);
				List<?> tableData = stream(datum.spliterator(), false).map(d -> structure.tableDataRow(d)).toList();
				TableUtils.renderTableData(structure != null ? structure.columns().toArray(Column[]::new) : null,
						tableData, displayMode, objectMapper, TableUtils.TableDataJsonPrettyPrinter.INSTANCE,
						System.out);
				if (displayMode == ResultDisplayMode.PRETTY && datum.getAuxiliary() != null
						&& !datum.getAuxiliary().isEmpty()) {
					tableData = datum.getAuxiliary().stream()
							.map(d -> datumAuxiliaryTableDataRow(d, objectMapper.writerWithDefaultPrettyPrinter()))
							.toList();
					TableUtils.renderTableData(datumAuxiliaryTableDataColumns(), tableData, displayMode, objectMapper,
							TableUtils.TableDataJsonPrettyPrinter.INSTANCE, System.out);
				}
			}
			return 0;
		} catch (Exception e) {
			System.err.println("Error listing datum: %s".formatted(e.getMessage()));
		}

		return 1;
	}

	private CloudIntegrationsFilter filter() {
		final CloudIntegrationsFilter filter = new CloudIntegrationsFilter();
		if (dateRange != null && dateRange.minDate != null) {
			filter.setStartDate(DateUtils.zonedDate(dateRange.minDate, zone));
		}
		if (dateRange != null && dateRange.maxDate != null) {
			filter.setEndDate(DateUtils.zonedDate(dateRange.maxDate, zone));
		}
		return filter;
	}

	/**
	 * List cloud datum stream datum.
	 * 
	 * @param restClient    the REST client
	 * @param objectMapper  the object mapper
	 * @param datumStreamId the datum stream ID to list datum for
	 * @param filter        an optional filter; if no start/end date range provided
	 *                      then the most recent data will be returned
	 * @return the result
	 * @throws IllegalStateException if an error occurs fetching the datum
	 */
	public static CloudDatumStreamQueryResult listCloudDatumStreamDatum(RestClient restClient,
			ObjectMapper objectMapper, Long datumStreamId, CloudIntegrationsFilter filter) {
		final boolean latest = (filter == null || !filter.hasDateRange());
		final String action = (latest ? "latest-datum" : "datum");
		// @formatter:off
		JsonNode response = restClient.get()
			.uri(b -> {
				b.path("/solaruser/api/v1/sec/user/c2c/datum-streams/{datumStreamId}/{action}");
				if (!latest) {
					b.queryParam("startDate", filter.startDate());
					b.queryParam("endDate", filter.endDate());
				}
				return b.build(datumStreamId, action);
			})
			.accept(MediaType.APPLICATION_JSON)
			.retrieve()
			.body(JsonNode.class)
			;		
		// @formatter:on

		checkSuccess(response);

		final JsonNode dataNode = response.path("data");

		try {
			if (latest) {
				List<Datum> result = new ArrayList<>(dataNode.size());
				for (JsonNode node : dataNode) {
					Datum conf;
					conf = objectMapper.treeToValue(node, Datum.class);
					if (conf != null) {
						result.add(conf);
					}
				}
				return new BasicCloudDatumStreamQueryResult(result);
			}
			return objectMapper.treeToValue(dataNode, BasicCloudDatumStreamQueryResult.class);
		} catch (JsonProcessingException | IllegalArgumentException e) {
			throw new IllegalStateException("Error parsing cloud datum stream datum list response: " + e.getMessage(),
					e);
		}
	}

	/**
	 * Get datum auxiliary info tabular structure columns.
	 * 
	 * @return the columns
	 */
	public static Column[] datumAuxiliaryTableDataColumns() {
		// @formatter:off
		return new Column[] {
				new Column().header("Timestamp").dataAlign(LEFT),
				new Column().header("Kind").dataAlign(LEFT),
				new Column().header("Object ID").dataAlign(RIGHT),
				new Column().header("Source ID").dataAlign(LEFT),
				new Column().header("Type").dataAlign(LEFT),
				new Column().header("Notes").dataAlign(LEFT),
				new Column().header("Metadata").dataAlign(LEFT),
			};
		// @formatter:on
	}

	/**
	 * Convert datum auxiliary listing into a tabular structure.
	 * 
	 * @param aux the configuration to convert
	 * @return the metadata data
	 */
	public static Object[] datumAuxiliaryTableDataRow(DatumAuxiliaryRecord aux, ObjectWriter jsonWriter) {
		try {
			// @formatter:off
			return new Object[] {
					aux.getTimestamp(),
					aux.getKind(),
					aux.getObjectId(),
					aux.getSourceId(),
					aux.getType().name(),
					aux.getNotes(),
					(aux.getMetadata() != null ? jsonWriter.writeValueAsString(aux.getMetadata()) : null)
				};
			// @formatter:on
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("Error rendering metadata as JSON: " + e.getMessage(), e);
		}
	}
}
