package s10k.tool.locations.cmd;

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

import net.solarnetwork.domain.BasicIdentityLocation;
import net.solarnetwork.domain.SimpleLocation;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.util.RestUtils;
import s10k.tool.common.util.StringUtils;
import s10k.tool.common.util.TableUtils;
import s10k.tool.locations.domain.LocationFilter;

/**
 * List locations.
 */
@Command(name = "list", sortSynopsis = false, showDefaultValues = true, descriptionHeading = "%n", optionListHeading = "%n", description = {
		"List location records matching search criteria.%n" })
public class ListLocationsCmd extends BaseSubCmd<LocationsGroup> implements Callable<Integer> {

	// @formatter:off
	@Option(names = { "-m", "--name" },
			description = "a case-insensitive keyword search, matching country, region, state, locality")
	@Nullable String name;
	
	@Option(names = { "-r", "--region" },
			description = "a region to match")
	@Nullable String region;
	
	@Option(names = { "-s", "--state", "--province" },
			description = "a state or province to match")
	@Nullable String stateOrProvince;
	
	@Option(names = { "-p", "--postal-code", "--zip-code" },
			description = "a postal code to match")
	@Nullable String postalCode;
	
	@Option(names = { "-c", "--country" },
			description = "a country to match")
	@Nullable String country;
	
	@Option(names = { "-tz", "--time-zone" },
			description = "a time zone to match")
	@Nullable ZoneId zone;
	
	@Option(names = {"-M", "--max"},
			description = "return at most this many results", paramLabel = "max")
	int maxResults;

	@Option(names = {"-O", "--offset"},
			description = "start returning results from this offset, 0 being the first result")
	long resultOffset;

	@Option(names = { "-sort", "--sort-by" },
			description = "sort the results; one of Source, Name, Country, Region, StateOrProvince, PostalCode, TimeZoneId",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "orderKey")
	String @Nullable [] orderBys;
	
	@Option(names = { "-mode", "--display-mode" },
			description = "how to display the data")
	@Nullable ResultDisplayMode displayMode;
	// @formatter:on

	/**
	 * Order by keys.
	 */
	public enum OrderBy {
		Source, Name, Country, Region, StateOrProvince, PostalCode, TimeZoneId
	}

	/**
	 * Constructor.
	 * 
	 * @param reqFactory   the HTTP request factory to use
	 * @param objectMapper the mapper to use
	 */
	public ListLocationsCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
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

			final List<BasicIdentityLocation> locations = listLocations(restClient, objectMapper, filter);
			if (locations.isEmpty()) {
				System.err.println("No locations match the search criteria.");
				return 1;
			}

			final List<?> tableData = (displayMode == ResultDisplayMode.JSON ? locations
					: locations.stream().map(ListLocationsCmd::tableDataRow).toList());
			TableUtils.renderTableData(tableDataColumns(), tableData,
					tableConfig(this, displayMode, prettyStyle(), zone), objectMapper,
					TableUtils.TableDataJsonPrettyPrinter.INSTANCE, System.out);
			return 0;
		} catch (Exception e) {
			System.err.println("Error listing locations: %s".formatted(e.getMessage()));
		}
		return 1;
	}

	private LocationFilter filter() {
		final var filter = new LocationFilter();
		if (name != null) {
			loc(filter).setName(name);
		}
		if (country != null) {
			loc(filter).setCountry(country);
		}
		if (region != null) {
			loc(filter).setRegion(region);
		}
		if (stateOrProvince != null) {
			loc(filter).setStateOrProvince(stateOrProvince);
		}
		if (postalCode != null) {
			loc(filter).setPostalCode(postalCode);
		}
		if (zone != null) {
			loc(filter).setTimeZoneId(zone.getId());
		}
		filter.setOrderBy(StringUtils.orderByList(orderBys, OrderBy.class));
		if (maxResults > 0) {
			filter.setMax(maxResults);
		}
		if (resultOffset > 0) {
			filter.setOffset(resultOffset);
		}
		return filter;
	}

	private SimpleLocation loc(LocationFilter filter) {
		SimpleLocation loc = filter.getLocation();
		if (loc == null) {
			loc = new SimpleLocation();
			filter.setLocation(loc);
		}
		return loc;
	}

	/**
	 * List locations.
	 * 
	 * @param restClient   the REST client
	 * @param objectMapper the mapper to use
	 * @param filter       the search criteria
	 * @return the locations
	 */
	public static List<BasicIdentityLocation> listLocations(final RestClient restClient,
			final ObjectMapper objectMapper, final LocationFilter filter) {
		// @formatter:off
		final JsonNode response = checkSuccess(restClient.get()
				.uri(b -> {
					b.path("/solarquery/api/v1/sec/location");
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
			BasicIdentityLocation[] result = objectMapper.treeToValue(response.path("data").path("results"),
					BasicIdentityLocation[].class);
			return (result != null ? List.of(result) : List.of());
		} catch (JsonProcessingException | IllegalArgumentException e) {
			throw new IllegalStateException("Error parsing location list response: " + e.getMessage(), e);
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
				new Column().header("Country").dataAlign(LEFT),
				new Column().header("Region").dataAlign(LEFT),
				new Column().header("State/Province").dataAlign(LEFT),
				new Column().header("Time Zone").dataAlign(LEFT),
				new Column().header("Locality").dataAlign(LEFT),
				new Column().header("Postal Code").dataAlign(LEFT),
			};
		// @formatter:on
	}

	/**
	 * Convert a location into a tabular structure.
	 * 
	 * @param location the location to convert
	 * @return the tabular data
	 */
	public static Object[] tableDataRow(BasicIdentityLocation location) {
		// @formatter:off
		return new Object[] {
				location.getId(),
				location.getCountry(),
				location.getRegion(),
				location.getStateOrProvince(),
				location.getTimeZoneId(),
				location.getLocality(),
				location.getPostalCode(),
			};
		// @formatter:on
	}

}
