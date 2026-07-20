package s10k.tool.c2c.ds.cmd;

import static com.github.freva.asciitable.HorizontalAlign.LEFT;
import static com.github.freva.asciitable.HorizontalAlign.RIGHT;
import static s10k.tool.c2c.util.CloudIntegrationRestUtils.listCloudDatumStreams;
import static s10k.tool.c2c.util.CloudIntegrationsUtils.datumStreamServiceLocalizedName;
import static s10k.tool.common.util.RestUtils.checkSuccess;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.freva.asciitable.Column;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import s10k.tool.c2c.domain.CloudDatumStreamConfiguration;
import s10k.tool.c2c.domain.CloudDatumStreamMappingConfiguration;
import s10k.tool.c2c.domain.CloudDatumStreamMappingPropertyConfiguration;
import s10k.tool.c2c.domain.CloudIntegrationConfiguration;
import s10k.tool.c2c.domain.CloudIntegrationsFilter;
import s10k.tool.c2c.util.CloudIntegrationRestUtils;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.util.OutputUtils;
import s10k.tool.common.util.TableUtils;

/**
 * View Cloud Datum Stream details.
 */
@Component
@Command(name = "view", sortSynopsis = false)
public class ViewDatumStreamCmd extends BaseSubCmd<DatumStreamsCmd> implements Callable<Integer> {

	// @formatter:off
	@Option(names = { "-stream", "--stream-id" },
			description = "the ID of the datum stream to view the details of")
	Long datumStreamId;

	@Option(names = { "-node", "--node-id" },
			description = "a node ID to match")
	Long nodeId;

	@Option(names = { "-source", "--source-id" },
			description = "a source ID pattern to match")
	String sourceId;
	
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
	public ViewDatumStreamCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		final RestClient restClient = restClient();
		final CloudIntegrationsFilter filter = filter();
		if (filter.getDatumStreamId() == null && filter.getNodeId() == null && filter.getSourceId() == null) {
			System.err.println("At least one search criteria option is required.");
			return 1;
		}

		try {
			final CloudDatumStreamConfiguration datumStream = viewCloudDatumStream(restClient, objectMapper, filter);
			if (datumStream == null) {
				System.err.println("No datum stream matched your criteria.");
				return 0;
			}
			final CloudDatumStreamMappingConfiguration mapping = (datumStream.datumStreamMappingId() != null
					? viewCloudDatumStreamMapping(restClient, objectMapper, datumStream.datumStreamMappingId())
					: null);
			final CloudIntegrationConfiguration integration = (mapping != null
					? CloudIntegrationRestUtils.viewCloudIntegration(restClient, objectMapper, mapping.integrationId())
					: null);
			final List<CloudDatumStreamMappingPropertyConfiguration> properties = (mapping != null
					? listCloudDatumStreamMappingProperties(restClient, objectMapper, mapping.configId())
					: null);

			if (displayMode == ResultDisplayMode.JSON) {
				OutputUtils.writeJsonObject(objectMapper,
						mappingDetails(datumStream, mapping, integration, properties));
			} else {
				final List<CloudDatumStreamPropertyDetail> details = (properties == null || properties.isEmpty()
						? List.of(new CloudDatumStreamPropertyDetail(datumStream, mapping, integration, null))
						: properties.stream()
								.map(p -> new CloudDatumStreamPropertyDetail(datumStream, mapping, integration, p))
								.toList());
				final List<?> tableData = details.stream().map(c -> tableDataRow(c, false)).toList();
				TableUtils.renderTableData(tableDataColumns(), tableData, displayMode, objectMapper,
						TableUtils.TableDataJsonPrettyPrinter.INSTANCE, System.out);
			}
			return 0;
		} catch (Exception e) {
			System.err.println("Error viewing cloud datum stream: %s".formatted(e.getMessage()));
		}
		return 1;
	}

	private CloudIntegrationsFilter filter() {
		final CloudIntegrationsFilter filter = new CloudIntegrationsFilter();
		if (datumStreamId != null) {
			filter.setDatumStreamId(datumStreamId);
		}
		if (nodeId != null) {
			filter.setNodeId(nodeId);
		}
		if (sourceId != null) {
			filter.setSourceId(sourceId);
		}
		return filter;
	}

	/**
	 * A datum stream property detail record.
	 */
	@RegisterReflectionForBinding
	public record CloudDatumStreamPropertyDetail(CloudDatumStreamConfiguration datumStream,
			CloudDatumStreamMappingConfiguration mapping, CloudIntegrationConfiguration integration,
			CloudDatumStreamMappingPropertyConfiguration property) {

	}

	/**
	 * A datum stream mapping detail record.
	 */
	@RegisterReflectionForBinding
	public record CloudDatumStreamMappingDetail(CloudDatumStreamConfiguration datumStream,
			CloudDatumStreamMappingConfiguration mapping, CloudIntegrationConfiguration integration,
			List<CloudDatumStreamMappingPropertyConfiguration> properties) {

	}

	/**
	 * Create a mapping detail record.
	 * 
	 * @param datumStream the datum stream
	 * @param mapping     the mapping
	 * @param integration the integration
	 * @param properties  the optional properties
	 * @return the mapping detail
	 */
	public static CloudDatumStreamMappingDetail mappingDetails(CloudDatumStreamConfiguration datumStream,
			CloudDatumStreamMappingConfiguration mapping, CloudIntegrationConfiguration integration,
			List<CloudDatumStreamMappingPropertyConfiguration> properties) {
		var result = new CloudDatumStreamMappingDetail(datumStream, mapping, integration,
				properties != null && !properties.isEmpty() ? new ArrayList<>(properties.size()) : null);
		for (CloudDatumStreamMappingPropertyConfiguration property : properties) {
			result.properties.add(property);
		}
		return result;
	}

	/**
	 * Get integrations info tabular structure columns.
	 * 
	 * @return the columns
	 */
	public static Column[] tableDataColumns() {
		// @formatter:off
		return new Column[] {
				new Column().header("ID").dataAlign(RIGHT),
				new Column().header("Name").dataAlign(LEFT),
				new Column().header("Type").dataAlign(LEFT),
				new Column().header("Enabled").dataAlign(LEFT),
				new Column().header("Kind").dataAlign(LEFT),
				new Column().header("Object ID").dataAlign(RIGHT),
				new Column().header("Source ID").dataAlign(LEFT),
				new Column().header("Schedule").dataAlign(LEFT),
				new Column().header("Mapping ID").dataAlign(RIGHT),
				new Column().header("Mapping Name").dataAlign(LEFT),
				new Column().header("Integration ID").dataAlign(RIGHT),
				new Column().header("Integration Name").dataAlign(LEFT),
				new Column().header("Integration Enabled").dataAlign(LEFT),
				new Column().header("Property #").dataAlign(RIGHT),
				new Column().header("Property Enabled").dataAlign(LEFT),
				new Column().header("Property Type").dataAlign(LEFT),
				new Column().header("Property Name").dataAlign(LEFT),
				new Column().header("Value Type").dataAlign(LEFT),
				new Column().header("Value Reference").dataAlign(LEFT),
			};
		// @formatter:on
	}

	/**
	 * Convert integrations listing into a tabular structure.
	 * 
	 * @param conf           the configuration to convert
	 * @param rawIdentifiers {@code true} to output the raw service identifiers
	 * @return the metadata data
	 */
	public static Object[] tableDataRow(CloudDatumStreamPropertyDetail conf, boolean rawIdentifiers) {
		// @formatter:off
		return new Object[] {
				conf.datumStream.configId(),
				conf.datumStream.name(),
				(rawIdentifiers 
						? conf.datumStream.serviceIdentifier()
						: datumStreamServiceLocalizedName(conf.datumStream.serviceIdentifier())),
				conf.datumStream.enabled(),
				(conf.datumStream.kind() != null ? conf.datumStream.kind().keyValue() : null),
				conf.datumStream.objectId(),
				conf.datumStream.sourceIdsValue(),
				conf.datumStream.schedule(),
				conf.datumStream.datumStreamMappingId(),
				(conf.mapping != null ? conf.mapping.name() : null),
				(conf.mapping != null ? conf.mapping.integrationId() : null),
				(conf.integration != null ? conf.integration.name() : null),
				(conf.integration != null ? conf.integration.enabled() : null),
				(conf.property != null ? conf.property.index() : null),
				(conf.property != null ? conf.property.enabled() : null),
				(conf.property != null ? conf.property.propertyType() : null),
				(conf.property != null ? conf.property.propertyName() : null),
				(conf.property != null ? conf.property.valueType() : null),
				(conf.property != null ? conf.property.valueReference() : null),
			};
		// @formatter:on
	}

	/**
	 * View a cloud datum stream.
	 * 
	 * @param restClient   the REST client
	 * @param objectMapper the object mapper
	 * @param filter       the search criteria; if a {@code datumStreamId} is
	 *                     available this will call
	 *                     {@link CloudIntegrationRestUtils#viewCloudDatumStream(RestClient, ObjectMapper, Long)};
	 *                     otherwise
	 *                     {@link CloudIntegrationRestUtils#listCloudDatumStreams(RestClient, ObjectMapper, CloudIntegrationsFilter)}
	 *                     will be called and the first result returned
	 * @return the first available cloud datum stream matching the search criteria
	 */
	public static CloudDatumStreamConfiguration viewCloudDatumStream(RestClient restClient, ObjectMapper objectMapper,
			CloudIntegrationsFilter filter) {
		if (filter.getDatumStreamId() != null) {
			return CloudIntegrationRestUtils.viewCloudDatumStream(restClient, objectMapper, filter.getDatumStreamId());
		}
		List<CloudDatumStreamConfiguration> confs = listCloudDatumStreams(restClient, objectMapper, filter);
		if (confs != null && !confs.isEmpty()) {
			return confs.getFirst();
		}
		return null;
	}

	/**
	 * View a cloud datum stream mapping.
	 * 
	 * @param restClient   the REST client
	 * @param objectMapper the object mapper
	 * @param mappingId    the mapping ID to view
	 * @return the result
	 */
	public static CloudDatumStreamMappingConfiguration viewCloudDatumStreamMapping(RestClient restClient,
			ObjectMapper objectMapper, Long mappingId) {
		// @formatter:off
		JsonNode response = restClient.get()
			.uri(b -> b.path("/solaruser/api/v1/sec/user/c2c/datum-stream-mappings/{datumStreamMappingId}")
				.build(mappingId)
			)
			.accept(MediaType.APPLICATION_JSON)
			.retrieve()
			.body(JsonNode.class)
			;		
		// @formatter:on

		checkSuccess(response);

		try {
			return objectMapper.treeToValue(response.path("data"), CloudDatumStreamMappingConfiguration.class);
		} catch (JsonProcessingException | IllegalArgumentException e) {
			throw new IllegalStateException("Error parsing cloud datum stream mapping response: " + e.getMessage(), e);
		}
	}

	/**
	 * View a cloud datum stream mapping properties.
	 * 
	 * @param restClient   the REST client
	 * @param objectMapper the object mapper
	 * @param mappingId    the mapping ID to view
	 * @return the result
	 */
	public static List<CloudDatumStreamMappingPropertyConfiguration> listCloudDatumStreamMappingProperties(
			RestClient restClient, ObjectMapper objectMapper, Long mappingId) {
		// @formatter:off
		JsonNode response = restClient.get()
			.uri(b -> b.path("/solaruser/api/v1/sec/user/c2c/datum-stream-mappings/{datumStreamMappingId}/properties")
				.build(mappingId)
			)
			.accept(MediaType.APPLICATION_JSON)
			.retrieve()
			.body(JsonNode.class)
			;		
		// @formatter:on

		checkSuccess(response);

		List<CloudDatumStreamMappingPropertyConfiguration> result = new ArrayList<>(response.path("data").size());
		for (JsonNode node : response.path("data").path("results")) {
			CloudDatumStreamMappingPropertyConfiguration conf;
			try {
				conf = objectMapper.treeToValue(node, CloudDatumStreamMappingPropertyConfiguration.class);
			} catch (JsonProcessingException | IllegalArgumentException e) {
				throw new IllegalStateException(
						"Error parsing cloud datum stream mapping property list response: " + e.getMessage(), e);
			}
			if (conf != null) {
				result.add(conf);
			}
		}
		return result;
	}

}
