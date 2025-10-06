package s10k.tool.datum.stream.cmd;

import static net.solarnetwork.domain.datum.DatumSamplesType.Accumulating;
import static net.solarnetwork.domain.datum.DatumSamplesType.Instantaneous;
import static net.solarnetwork.domain.datum.DatumSamplesType.Status;
import static org.springframework.util.StringUtils.arrayToCommaDelimitedString;
import static s10k.tool.common.util.RestUtils.checkSuccess;
import static s10k.tool.datum.stream.cmd.ListDatumStreamMetadataCmd.metadataColumns;
import static s10k.tool.datum.stream.cmd.ListDatumStreamMetadataCmd.metadataRow;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import net.solarnetwork.domain.datum.BasicObjectDatumStreamMetadata;
import net.solarnetwork.domain.datum.ObjectDatumKind;
import net.solarnetwork.domain.datum.ObjectDatumStreamMetadata;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.util.OutputUtils;
import s10k.tool.common.util.TableUtils;

/**
 * Rename node datum stream IDs and/or property names.
 */
@Component
@Command(name = "rename", sortSynopsis = false, header = {
// @formatter:off
		"",
		"""
		Rename node datum stream IDs and property names.
		""",
		// @formatter:on
})
public class RenameDatumStreamMetadataCmd extends BaseSubCmd<DatumStreamCmd> implements Callable<Integer> {

	// @formatter:off
	@Option(names = { "-stream", "--stream-id" },
			description = "the ID of the datum stream to rename the attributes of",
			required = true)
	UUID streamId;
	
	@Option(names = { "-node", "--node-id" },
			description = "a node ID to set")
	Long nodeId;

	@Option(names = { "-source", "--source-id" },
			description = "a source ID to set")
	String sourceId;

	@Option(names = { "-i", "--instantaneous" },
			description = "instantaneous property names to set",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "propName")
	String[] instantaneousPropertyNames;
	
	@Option(names = { "-a", "--accumulating" },
			description = "accumulating property names to set",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "propName")
	String[] accumulatingPropertyNames;
	
	@Option(names = { "-s", "--status" },
			description = "status property names to set",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "propName")
	String[] statusPropertyNames;	

	@Option(names = { "-mode", "--display-mode" },
			description = "how to display the data",
			defaultValue = "PRETTY")
	ResultDisplayMode displayMode = ResultDisplayMode.PRETTY;
	// @formatter:on

	/**
	 * Constructor.
	 * 
	 * @param reqFactory   the HTTP request factory to use
	 * @param objectMapper the mapper to use
	 */
	public RenameDatumStreamMetadataCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	private static final Long UNSPECIFIED_LONG_ID = 0L;
	private static final String UNSPECIFIED_STRING_ID = "";

	@Override
	public Integer call() throws Exception {
		if (nodeId == null && (sourceId == null || sourceId.isBlank())
				&& (instantaneousPropertyNames == null || instantaneousPropertyNames.length < 1)
				&& (accumulatingPropertyNames == null || accumulatingPropertyNames.length < 1)
				&& (statusPropertyNames == null || statusPropertyNames.length < 1)) {
			System.err.println("Nothing provided to update.");
			return 2;
		}
		final var streamMeta = new BasicObjectDatumStreamMetadata(streamId, null, ObjectDatumKind.Node,
				nodeId != null ? nodeId : UNSPECIFIED_LONG_ID, sourceId != null ? sourceId : UNSPECIFIED_STRING_ID,
				instantaneousPropertyNames, accumulatingPropertyNames, statusPropertyNames);

		final RestClient restClient = restClient();
		try {
			ObjectDatumStreamMetadata result = updateNodeDatumStreamAttributes(restClient, objectMapper, streamMeta);

			if (displayMode == ResultDisplayMode.JSON) {
				OutputUtils.writeJsonObject(objectMapper, result);
			} else {
				List<?> tableData = Collections.singletonList(result).stream().map(m -> metadataRow(m, true)).toList();
				TableUtils.renderTableData(metadataColumns(), tableData, displayMode, objectMapper,
						TableUtils.TableDataJsonPrettyPrinter.INSTANCE, System.out);
			}
			return 0;
		} catch (Exception e) {
			System.err.println("Error renaming datum stream attributes: %s".formatted(e.getMessage()));
		}
		return 1;
	}

	/**
	 * Update datum stream IDs or property names.
	 * 
	 * @param restClient   the REST client
	 * @param objectMapper the object mapper
	 * @param streamMeta   the stream attributes to update; must provide a
	 *                     {@code streamId}
	 * @return the updated stream info
	 */
	public static ObjectDatumStreamMetadata updateNodeDatumStreamAttributes(final RestClient restClient,
			ObjectMapper objectMapper, final ObjectDatumStreamMetadata streamMeta) {
		assert streamMeta != null && streamMeta.getStreamId() != null;

		MultiValueMap<String, Object> postBody = new LinkedMultiValueMap<>(1);
		if (streamMeta.getObjectId() != null && !UNSPECIFIED_LONG_ID.equals(streamMeta.getObjectId())) {
			postBody.add("nodeId", streamMeta.getObjectId());
		}
		if (streamMeta.getSourceId() != null && !UNSPECIFIED_STRING_ID.equals(streamMeta.getSourceId())) {
			postBody.add("sourceId", streamMeta.getSourceId());
		}
		if (streamMeta.propertyNamesForType(Instantaneous) != null) {
			postBody.add("i", arrayToCommaDelimitedString(streamMeta.propertyNamesForType(Instantaneous)));
		}
		if (streamMeta.propertyNamesForType(Accumulating) != null) {
			postBody.add("a", arrayToCommaDelimitedString(streamMeta.propertyNamesForType(Accumulating)));
		}
		if (streamMeta.propertyNamesForType(Status) != null) {
			postBody.add("s", arrayToCommaDelimitedString(streamMeta.propertyNamesForType(Status)));
		}

		try {
			// @formatter:off
			final JsonNode response = restClient.post()
				.uri("/solaruser/api/v1/sec/datum/stream/meta/node/" +streamMeta.getStreamId())
				.body(postBody)
				.contentType(MediaType.APPLICATION_FORM_URLENCODED)
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.body(JsonNode.class)
				;		
			// @formatter:on

			checkSuccess(response);

			return objectMapper.treeToValue(response.path("data"), ObjectDatumStreamMetadata.class);
		} catch (JsonProcessingException e) {
			throw new IllegalStateException("Error parsing datum stream metadata response: " + e.getMessage(), e);
		}
	}

}
