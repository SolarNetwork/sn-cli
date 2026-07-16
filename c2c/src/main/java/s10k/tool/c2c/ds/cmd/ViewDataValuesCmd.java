package s10k.tool.c2c.ds.cmd;

import static com.github.freva.asciitable.HorizontalAlign.LEFT;
import static java.util.stream.Collectors.joining;
import static s10k.tool.c2c.util.CloudIntegrationRestUtils.viewCloudDatumStream;
import static s10k.tool.c2c.util.CloudIntegrationRestUtils.viewDatumStreamFilters;
import static s10k.tool.c2c.util.CloudIntegrationsUtils.findDatumStreamServiceId;
import static s10k.tool.common.util.RestUtils.checkSuccess;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SequencedMap;
import java.util.concurrent.Callable;

import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.freva.asciitable.Column;

import net.solarnetwork.codec.JsonUtils;
import net.solarnetwork.util.StringUtils;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import s10k.tool.c2c.domain.CloudDataValue;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.util.RestUtils;
import s10k.tool.common.util.TableUtils;

/**
 * View datum stream data values.
 */
@Component
@Command(name = "data-values", aliases = "dv", sortSynopsis = false)
public class ViewDataValuesCmd extends BaseSubCmd<DatumStreamsCmd> implements Callable<Integer> {

	// @formatter:off
	@Option(names = { "-i", "--integration-id" },
			description = "the Cloud Integration ID to view datum data values for",
			required =  true)
	Long integrationId;

	@ArgGroup(exclusive = true, multiplicity = "0..1")
	StreamTypeOrId streamTypeOrId;

	@Option(names = { "-p", "--path" },
			description = "the datum data value path to return")
	String path;

	@Option(names = { "-mode", "--display-mode" },
			description = "how to display the data",
			defaultValue = "PRETTY")
	ResultDisplayMode displayMode = ResultDisplayMode.PRETTY;
	// @formatter:on

	/**
	 * Grouping of stream type or ID, where only one or the other should be
	 * specified.
	 */
	static class StreamTypeOrId {
		// @formatter:off
		@Option(names = { "-t", "--stream-type" },
				description = "the datum stream type to show data values for")
		String type;
		

		@Option(names = { "-stream", "--stream-id" },
				description = "the ID of the datum stream to show data value for")
    	Long datumStreamId;
    	// @formatter:on

		/**
		 * Test if a datum stream ID is configured.
		 * 
		 * @return {@code true} if datumStreamId is configured
		 */
		boolean isDatumStream() {
			return datumStreamId != null;
		}

	}

	/**
	 * Constructor.
	 * 
	 * @param reqFactory   the HTTP request factory to use
	 * @param objectMapper the mapper to use
	 */
	public ViewDataValuesCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		final RestClient restClient = restClient();

		final List<String> pathIdentifiers = StringUtils.delimitedStringToList(path, "/");

		if (pathIdentifiers != null && !pathIdentifiers.isEmpty() && streamTypeOrId == null) {
			System.err.println("The --stream-type option must be provided when --path specified.");
			return 1;
		}

		// determine datum stream service ID (if needed)
		final String datumStreamServiceId;
		try {
			if (streamTypeOrId != null) {
				if (streamTypeOrId.datumStreamId != null) {
					datumStreamServiceId = viewCloudDatumStream(restClient, objectMapper, streamTypeOrId.datumStreamId)
							.serviceIdentifier();
				} else {
					datumStreamServiceId = findDatumStreamServiceId(streamTypeOrId.type).getKey();
				}
			} else {
				datumStreamServiceId = null;
			}
		} catch (IllegalStateException e) {
			System.err.printf("Error looking up stream type information: %s", e.getMessage());
			return 1;
		}
		if (pathIdentifiers != null && !pathIdentifiers.isEmpty() && datumStreamServiceId == null) {
			System.err.printf("The stream type [%s] is not supported.\n", streamTypeOrId.type);
			return 1;
		}

		try {
			List<CloudDataValue> confs = viewDatumDataValues(restClient, objectMapper, integrationId,
					(streamTypeOrId != null ? streamTypeOrId.datumStreamId : null),
					(datumStreamServiceId != null ? datumStreamServiceId : null), pathIdentifiers);
			if (confs.isEmpty()) {
				System.err.println("No sources matched your criteria.");
				return 0;
			}

			List<?> tableData = (displayMode == ResultDisplayMode.JSON ? confs
					: CloudDataValue.flatList(confs).stream().map(c -> tableDataRow(c)).toList());
			TableUtils.renderTableData(tableDataColumns(), tableData, displayMode, objectMapper,
					TableUtils.TableDataJsonPrettyPrinter.INSTANCE, System.out);
			return 0;
		} catch (Exception e) {
			System.err.println("Error viewing cloud data values: %s".formatted(e.getMessage()));
		}
		return 1;
	}

	/**
	 * Get integrations info tabular structure columns.
	 * 
	 * @return the columns
	 */
	public static Column[] tableDataColumns() {
		// @formatter:off
		return new Column[] {
				new Column().header("Name").dataAlign(LEFT),
				new Column().header("Identifiers").dataAlign(LEFT),
				new Column().header("Reference").dataAlign(LEFT),
				new Column().header("Metadata").dataAlign(LEFT),
			};
		// @formatter:on
	}

	/**
	 * Convert data value into a tabular structure.
	 * 
	 * @param dataValue the data value to convert
	 * @return the tabular data
	 */
	public static Object[] tableDataRow(CloudDataValue dataValue) {
		// @formatter:off
		return new Object[] {
				dataValue.getName(),
				dataValue.getIdentifiers().stream().collect(joining("\n")),
				dataValue.getReference(),
				TableUtils.basicTable(dataValue.getMetadata(), null, null, false),
			};
		// @formatter:on
	}

	/**
	 * View datum data values.
	 * 
	 * @param restClient           the REST client
	 * @param objectMapper         the object mapper
	 * @param integrationId        the integration ID
	 * @param datumStreamId        the optional datum stream ID (required by some
	 *                             providers like eGauge)
	 * @param datumStreamServiceId the datum stream service ID; required if
	 *                             {@code pathIdentifiers} is provided or the
	 *                             integration offers more than one datum stream
	 *                             service
	 * @param pathIdentifiers      the optional path identifiers to extract
	 * @return the result
	 */
	public static List<CloudDataValue> viewDatumDataValues(RestClient restClient, ObjectMapper objectMapper,
			Long integrationId, Long datumStreamId, String datumStreamServiceId, List<String> pathIdentifiers) {

		final MultiValueMap<String, String> queryParameters = new LinkedMultiValueMap<>(
				(pathIdentifiers != null ? pathIdentifiers.size() : 0) + (datumStreamId != null ? 1 : 0)
						+ (datumStreamServiceId != null ? 1 : 0));
		if (datumStreamServiceId != null) {
			queryParameters.put("datumStreamServiceIdentifier", List.of(datumStreamServiceId));
		}
		if (datumStreamId != null) {
			queryParameters.put("datumStreamId", List.of(datumStreamId.toString()));
		}
		if (pathIdentifiers != null && !pathIdentifiers.isEmpty()) {
			// get datum stream filter names for given service type
			SequencedMap<String, String> filterKeys = viewDatumStreamFilters(restClient, objectMapper,
					datumStreamServiceId);

			// skip past leading empty path identifiers
			int i = 0;
			for (; i < pathIdentifiers.size(); i++) {
				if (!pathIdentifiers.get(i).isEmpty()) {
					break;
				}
			}
			for (String filterId : filterKeys.keySet()) {
				if (i >= pathIdentifiers.size()) {
					break;
				}
				String filterVal = pathIdentifiers.get(i);
				if (filterVal != null && !filterVal.isEmpty()) {
					queryParameters.put(filterId, List.of(filterVal));
				}
				i++;
			}
		}

		// @formatter:off
		JsonNode response = restClient.get()
			.uri(b -> {
				b.path("/solaruser/api/v1/sec/user/c2c/integrations/{integrationId}/datum-data-values");
				RestUtils.populateQueryParameters(b, () -> queryParameters);			
				return b.build(integrationId);
			})
			.accept(MediaType.APPLICATION_JSON)
			.retrieve()
			.body(JsonNode.class)
			;		
		// @formatter:on

		checkSuccess(response);

		final JsonNode data = response.path("data");
		if (data.isEmpty()) {
			return List.of();
		}

		final List<CloudDataValue> result = new ArrayList<>(data.size());
		try {
			for (JsonNode dataValueNode : data) {
				CloudDataValue dv = parseCloudDataValue(dataValueNode);
				if (dv != null) {
					result.add(dv);
				}
			}
			return result;
		} catch (IllegalArgumentException e) {
			throw new IllegalStateException("Error parsing datum data values response: " + e.getMessage(), e);
		}
	}

	private static CloudDataValue parseCloudDataValue(JsonNode dataValueNode) {
		final String name = dataValueNode.path("name").textValue();
		final String ref = dataValueNode.path("reference").textValue();
		final JsonNode identListNode = dataValueNode.path("identifiers");
		final List<String> identifiers = new ArrayList<>(identListNode.size());
		for (JsonNode identNode : identListNode) {
			String ident = identNode.textValue();
			if (ident != null) {
				identifiers.add(ident);
			}
		}
		final Map<String, Object> meta = JsonUtils.getStringMapFromTree(dataValueNode.path("metadata"));
		final JsonNode childrenNode = dataValueNode.path("children");
		final List<CloudDataValue> children;
		if (childrenNode.isArray() && !childrenNode.isEmpty()) {
			children = new ArrayList<>(childrenNode.size());
			for (JsonNode childDataNode : childrenNode) {
				CloudDataValue child = parseCloudDataValue(childDataNode);
				if (child != null) {
					children.add(child);
				}
			}
		} else {
			children = null;
		}
		return new CloudDataValue(identifiers, name, ref, meta, children);
	}

}
