package s10k.tool.locations.requests.cmd;

import static s10k.tool.common.util.RestUtils.checkSuccess;
import static s10k.tool.common.util.TableUtils.tableConfig;

import java.util.List;
import java.util.concurrent.Callable;

import org.jspecify.annotations.Nullable;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.util.TableUtils;
import s10k.tool.locations.domain.LocationRequest;

/**
 * View a location request.
 */
@Command(name = "view", sortSynopsis = false, showDefaultValues = true, descriptionHeading = "%n", optionListHeading = "%n", description = {
		"View a location request record.%n" })
public class ViewLocationRequestCmd extends BaseSubCmd<LocationsRequestsGroup> implements Callable<Integer> {

	// @formatter:off
	@Option(names = { "-r", "--request-id" },
			description = "the ID of the request to view",
			required = true)
	@SuppressWarnings("NullAway.Init")
	Long requestId;
	
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
	public ViewLocationRequestCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		try {
			final RestClient restClient = restClient();
			final ObjectMapper objectMapper = objectMapper();
			final ResultDisplayMode displayMode = displayMode(this.displayMode);

			final LocationRequest record = viewLocationRequest(restClient, objectMapper, requestId);

			final List<LocationRequest> records = List.of(record);
			final List<?> tableData = (displayMode == ResultDisplayMode.JSON ? records
					: records.stream().map(ListLocationRequestsCmd::tableDataRow).toList());
			TableUtils.renderTableData(ListLocationRequestsCmd.tableDataColumns(), tableData,
					tableConfig(this, displayMode, prettyStyle()).asJsonSingleton(), objectMapper,
					TableUtils.TableDataJsonPrettyPrinter.INSTANCE, System.out);
			return 0;
		} catch (Exception e) {
			System.err.println("Error viewing location request: %s".formatted(e.getMessage()));
		}
		return 1;
	}

	/**
	 * View a location request.
	 * 
	 * @param restClient   the REST client
	 * @param objectMapper the mapper to use
	 * @param requestId    the request ID to view
	 * @return the request
	 */
	public static LocationRequest viewLocationRequest(final RestClient restClient, final ObjectMapper objectMapper,
			final Long requestId) {
		// @formatter:off
		final JsonNode response = checkSuccess(restClient.get()
				.uri(b -> {
					b.path("/solaruser/api/v1/sec/location/meta/request/{id}");
					return b.build(requestId);
				})
			.accept(MediaType.APPLICATION_JSON)
			.retrieve()
			.body(JsonNode.class)
			);
		// @formatter:on

		try {
			return objectMapper.treeToValue(response.path("data"), LocationRequest.class);
		} catch (JsonProcessingException | IllegalArgumentException e) {
			throw new IllegalStateException("Error parsing location request view response: " + e.getMessage(), e);
		}
	}

}
