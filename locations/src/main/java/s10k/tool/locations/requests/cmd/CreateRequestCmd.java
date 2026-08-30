package s10k.tool.locations.requests.cmd;

import static java.nio.charset.StandardCharsets.UTF_8;
import static s10k.tool.common.util.RestUtils.checkSuccess;
import static s10k.tool.common.util.StringUtils.stringOrFileContents;
import static s10k.tool.common.util.TableUtils.tableConfig;

import java.io.InputStreamReader;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Callable;

import org.jspecify.annotations.Nullable;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.solarnetwork.codec.JsonUtils;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.MergeMode;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.util.CollectionUtils;
import s10k.tool.common.util.SystemUtils;
import s10k.tool.common.util.TableUtils;
import s10k.tool.locations.domain.LocationFeature;
import s10k.tool.locations.domain.LocationRequest;
import s10k.tool.locations.domain.LocationRequestInfo;
import s10k.tool.locations.domain.LocationRequestStatus;

/**
 * Submit a location request.
 */
@Command(name = "create", sortSynopsis = false, showDefaultValues = true, descriptionHeading = "%n", optionListHeading = "%n", description = {
		// @formatter:off
		"Create a location request. The various options can be used to configure specific settings of the request.%n",

		"Alternatively the request can be provided as JSON via standard input or via an @file.json parameter.%n", 
		// @formatter:on
})
public class CreateRequestCmd extends BaseSubCmd<LocationsRequestsCmd> implements Callable<Integer> {

	// @formatter:off
	@Option(names = { "-source", "--source-id" },
			description = "the location source ID")
	@Nullable String sourceId;

	@Option(names = { "-f", "--feature" },
			description = "a desired location feature",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "feature")
	LocationFeature @Nullable [] features;

	@Option(names = { "-m", "--name" },
			description = "a display name")
	@Nullable String name;

	@Option(names = { "-l", "--locality", "--city" },
			description = "a locality (city) name")
	@Nullable String locality;
	
	@Option(names = { "-r", "--region" },
			description = "a region name")
	@Nullable String region;
	
	@Option(names = { "-s", "--state", "--province" },
			description = "a state or province name")
	@Nullable String stateOrProvince;
	
	@Option(names = { "-p", "--postal-code", "--zip-code" },
			description = "a postal code")
	@Nullable String postalCode;
	
	@Option(names = { "-c", "--country" },
			description = "a 2-character country code")
	@Nullable String country;
	
	@Option(names = { "-tz", "--time-zone" },
			description = "a time zone")
	@Nullable ZoneId zone;

	@Option(names = {"-I", "--ignore-input"},
			description = "do not try to read from standard input")
	boolean ignoreStdIn;
	
	@Option(names = { "-mode", "--display-mode" },
			description = "how to display the data")
	@Nullable ResultDisplayMode displayMode;	

	@Parameters(index = "0",
			arity = "0..1",
			paramLabel = "<config>",
			description = "the configuraiton to use as a JSON object, or @file for file to load")
	@Nullable String value;
	// @formatter:on

	/**
	 * Constructor.
	 * 
	 * @param reqFactory   the HTTP request factory to use
	 * @param objectMapper the mapper to use
	 */
	public CreateRequestCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		try {
			final RestClient restClient = restClient();
			final ObjectMapper objectMapper = objectMapper();
			final ResultDisplayMode displayMode = displayMode(this.displayMode);

			final Map<String, Object> settings = new LinkedHashMap<>(4);

			// look for JSON on stdin if allowed
			if (!(ignoreStdIn || SystemUtils.systemConsoleIsTerminal())) {
				Map<String, Object> inputProps = objectMapper.readValue(new InputStreamReader(System.in, UTF_8),
						JsonUtils.STRING_MAP_TYPE);
				CollectionUtils.mergeServiceProperties(inputProps, settings, MergeMode.RecursiveObjects);
			}

			try {
				populateConfiguration(settings);
			} catch (RuntimeException e) {
				System.err.println(e.getMessage());
				return 1;
			}

			if (value != null && !value.isBlank()) {
				Map<String, Object> inputProps = objectMapper.readValue(stringOrFileContents(value),
						JsonUtils.STRING_MAP_TYPE);
				CollectionUtils.mergeServiceProperties(inputProps, settings, MergeMode.RecursiveObjects);
			}

			final LocationRequestInfo info = objectMapper.convertValue(settings, LocationRequestInfo.class);

			if (info.sourceId() == null || info.sourceId().isEmpty()) {
				System.err.println("A source ID is required (--source-id option).");
				return 1;
			} else if (info.features() == null || info.features().isEmpty()) {
				System.err.println("At least one feature is required (--feature option).");
				return 1;
			} else if (info.location() == null) {
				System.err.println("Location info is required.");
				return 1;
			} else if (info.location().getName() == null || info.location().getName().isEmpty()) {
				System.err.println("Name is required (--name option).");
				return 1;
			} else if (info.location().getCountry() == null || info.location().getCountry().isEmpty()) {
				System.err.println("Country is required (--country option).");
				return 1;
			} else if (info.location().getTimeZoneId() == null || info.location().getTimeZoneId().isEmpty()) {
				System.err.println("Time zone is required (--time-zone option).");
				return 1;
			} else if ((info.location().getRegion() == null || info.location().getRegion().isEmpty())
					&& (info.location().getStateOrProvince() == null
							|| info.location().getStateOrProvince().isEmpty())) {
				System.err.println("Region or state or province is required (--region and --state options).");
				return 1;
			} else if (info.location().getLocality() == null || info.location().getLocality().isEmpty()) {
				System.err.println("Locality is required (--locality option).");
				return 1;
			}

			final LocationRequest record;
			if (!isDryRun()) {
				record = createLocationRequest(restClient, objectMapper, info);
			} else {
				var now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
				record = new LocationRequest(-1L, now, now, -1L, LocationRequestStatus.Submitted, null, null,
						JsonUtils.getStringMapFromObject(info));
			}

			final List<LocationRequest> records = List.of(record);
			final List<?> tableData = (displayMode == ResultDisplayMode.JSON ? records
					: records.stream().map(ListLocationRequestsCmd::tableDataRow).toList());
			TableUtils.renderTableData(ListLocationRequestsCmd.tableDataColumns(), tableData,
					tableConfig(this, displayMode, prettyStyle()).asJsonSingleton(), objectMapper,
					TableUtils.TableDataJsonPrettyPrinter.INSTANCE, System.out);
			return 0;
		} catch (Exception e) {
			System.err.println("Error creating location request: %s".formatted(e.getMessage()));
		}
		return 1;
	}

	private void populateConfiguration(Map<String, Object> settings) {
		if (sourceId != null) {
			settings.put("sourceId", sourceId);
		}
		if (features != null && features.length > 0) {
			settings.put("features", Arrays.stream(features).map(f -> f.name().toLowerCase(Locale.ENGLISH)).toList());
		}

		Map<String, String> locSettings = new LinkedHashMap<String, String>(8);
		settings.put("location", locSettings);

		if (name != null) {
			locSettings.put("name", name);
		}
		if (locality != null) {
			locSettings.put("locality", locality);
		}
		if (region != null) {
			locSettings.put("region", region);
		}
		if (stateOrProvince != null) {
			locSettings.put("stateOrProvince", stateOrProvince);
		}
		if (postalCode != null) {
			locSettings.put("postalCode", postalCode);
		}
		if (country != null) {
			locSettings.put("country", country);
		}
		if (zone != null) {
			locSettings.put("zone", zone.getId());
		}
	}

	/**
	 * Create a location request.
	 * 
	 * @param restClient   the REST client
	 * @param objectMapper the mapper to use
	 * @param info         the request info
	 * @return the requests
	 */
	public static LocationRequest createLocationRequest(final RestClient restClient, final ObjectMapper objectMapper,
			final LocationRequestInfo info) {
		// @formatter:off
		final JsonNode response = checkSuccess(restClient.post()
				.uri(b -> {
					b.path("/solaruser/api/v1/sec/location/meta/request");
					return b.build();
				})
				.contentType(MediaType.APPLICATION_JSON)
				.body(info)
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.body(JsonNode.class)
				);
		// @formatter:on

		try {
			return objectMapper.treeToValue(response.path("data"), LocationRequest.class);
		} catch (JsonProcessingException | IllegalArgumentException e) {
			throw new IllegalStateException("Error parsing location request create response: " + e.getMessage(), e);
		}
	}

}
