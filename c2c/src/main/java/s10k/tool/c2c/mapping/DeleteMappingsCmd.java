package s10k.tool.c2c.mapping;

import static s10k.tool.c2c.util.CloudIntegrationRestUtils.listCloudDatumStreamMappings;
import static s10k.tool.common.util.RestUtils.checkSuccess;

import java.util.List;
import java.util.concurrent.Callable;

import org.jspecify.annotations.Nullable;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import s10k.tool.c2c.domain.CloudDatumStreamMappingConfiguration;
import s10k.tool.c2c.domain.CloudIntegrationsFilter;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.ResultDisplayMode;

/**
 * Delete Cloud Datum Stream Mapping configurations.
 */
@Command(name = "delete", sortSynopsis = false, showDefaultValues = true, descriptionHeading = "%n", optionListHeading = "%n", description = {
		// @formatter:off
		"Delete cloud datum stream mappings by their ID.%n",

		"When deleting a mapping, all associated properties are deleted as well, and any cloud datum streams referencing the deleted mapping will have their mapping association nullified.", 
		// @formatter:on
})
public class DeleteMappingsCmd extends BaseSubCmd<MappingsGroup> implements Callable<Integer> {

	// @formatter:off
	@Option(names = { "-map", "--mapping-id" },
			description = "a datum stream mapping ID to delete",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "mappingId")
	Long @Nullable [] mappingIds;
		
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
	public DeleteMappingsCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		try {
			final RestClient restClient = restClient();
			final ObjectMapper objectMapper = objectMapper();
			final ResultDisplayMode displayMode = displayMode(this.displayMode);

			final var filter = new CloudIntegrationsFilter();
			filter.setDatumStreamMappingIds(List.of(mappingIds));

			final List<CloudDatumStreamMappingConfiguration> confs = listCloudDatumStreamMappings(restClient,
					objectMapper, filter);
			if (confs.isEmpty()) {
				System.err.println("No datum stream mappings matched your criteria.");
				return 0;
			}

			if (!isDryRun()) {
				for (CloudDatumStreamMappingConfiguration stream : confs) {
					deleteCloudDatumStreamMapping(restClient, stream.configId());
				}
			}

			ListMappingsCmd.renderMappings(restClient, objectMapper, this, displayMode, prettyStyle(), confs);
			return 0;
		} catch (Exception e) {
			System.err.println("Error deleting cloud datum stream mappings: %s".formatted(e.getMessage()));
		}
		return 1;
	}

	/**
	 * Delete a cloud datum stream.
	 * 
	 * @param restClient the REST client
	 * @param mappingId  the datum stream ID to delete
	 * @throws IllegalStateException if an error occurs fetching the stream
	 */
	public static void deleteCloudDatumStreamMapping(RestClient restClient, Long mappingId) {
		// @formatter:off
		checkSuccess(restClient.delete()
			.uri(b -> b.path("/solaruser/api/v1/sec/user/c2c/datum-stream-mappings/{mappingId}")
				.build(mappingId)
			)
			.accept(MediaType.APPLICATION_JSON)
			.retrieve()
			.body(JsonNode.class)
			);		
		// @formatter:on
	}

}
