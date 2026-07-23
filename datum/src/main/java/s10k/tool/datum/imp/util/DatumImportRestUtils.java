package s10k.tool.datum.imp.util;

import static s10k.tool.common.util.RestUtils.checkSuccess;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import s10k.tool.common.util.RestUtils;
import s10k.tool.datum.imp.domain.DatumImportTaskInfo;
import s10k.tool.datum.imp.domain.DatumImportsFilter;

/**
 * REST helper methods for datum import.
 */
public final class DatumImportRestUtils {

	private DatumImportRestUtils() {
		// not available
	}

	/**
	 * List datum import tasks.
	 * 
	 * @param restClient   the REST client
	 * @param objectMapper the object mapper
	 * @param filter       the criteria
	 * @return the list of matching tasks
	 * @throws IllegalStateException if an error occurs
	 */
	public static List<DatumImportTaskInfo> listDatumImportTasks(RestClient restClient, ObjectMapper objectMapper,
			DatumImportsFilter filter) {
		// @formatter:off
		final JsonNode response = checkSuccess(restClient.get()
				.uri(b -> {
					b.path("/solaruser/api/v1/sec/user/import/jobs");
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
			DatumImportTaskInfo[] result = objectMapper.treeToValue(response.path("data"), DatumImportTaskInfo[].class);
			return (result != null ? List.of(result).stream().map(DatumImportTaskInfo::normalized).toList()
					: List.of());
		} catch (JsonProcessingException | IllegalArgumentException e) {
			throw new IllegalStateException("Error parsing datum import task list response: " + e.getMessage(), e);
		}
	}

	/**
	 * View a datum import task.
	 * 
	 * @param restClient   the REST client
	 * @param objectMapper the object mapper
	 * @param jobId        the staged job ID to preview
	 * @return the updated job info
	 * @throws IllegalStateException if an error occurs
	 */
	public static DatumImportTaskInfo viewDatumImportTask(RestClient restClient, ObjectMapper objectMapper,
			String jobId) {
		// @formatter:off
			final JsonNode response = checkSuccess(restClient.get()
					.uri(b -> {
						b.path("/solaruser/api/v1/sec/user/import/jobs/{jobId}");
						return b.build(jobId);
					})
				.accept(MediaType.APPLICATION_JSON)
				.retrieve()
				.body(JsonNode.class)
				);		
			// @formatter:on

		try {
			return objectMapper.treeToValue(response.path("data"), DatumImportTaskInfo.class).normalized();
		} catch (JsonProcessingException | IllegalArgumentException e) {
			throw new IllegalStateException("Error parsing confirm datum import response: " + e.getMessage(), e);
		}
	}

}
