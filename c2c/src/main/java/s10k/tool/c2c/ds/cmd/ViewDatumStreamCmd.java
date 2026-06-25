package s10k.tool.c2c.ds.cmd;

import static com.github.freva.asciitable.HorizontalAlign.LEFT;
import static com.github.freva.asciitable.HorizontalAlign.RIGHT;
import static java.util.stream.Collectors.joining;
import static net.solarnetwork.util.StringNaturalSortComparator.CASE_INSENSITIVE_NATURAL_SORT;
import static s10k.tool.c2c.util.CloudIntegrationsUtils.datumStreamServiceLocalizedName;
import static s10k.tool.common.util.RestUtils.checkSuccess;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

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
import s10k.tool.c2c.i9n.cmd.ListIntegrationsCmd;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.util.TableUtils;

/**
 * View Cloud Datum Stream details.
 */
@Component
@Command(name = "view", sortSynopsis = false)
public class ViewDatumStreamCmd extends BaseSubCmd<DatumStreamsCmd> implements Callable<Integer> {

	// @formatter:off
	@Option(names = { "-id", "--id" },
			description = "the ID of the datum stream to view the details of",
			required = true)
	Long datumStreamId;

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

		try {
			final CloudDatumStreamConfiguration datumStream = viewCloudDatumStream(restClient, objectMapper,
					datumStreamId);
			final CloudDatumStreamMappingConfiguration mapping = (datumStream.datumStreamMappingId() != null
					? viewCloudDatumStreamMapping(restClient, objectMapper, datumStream.datumStreamMappingId())
					: null);
			final CloudIntegrationConfiguration integration = (mapping != null
					? ListIntegrationsCmd.viewCloudIntegration(restClient, objectMapper, mapping.integrationId())
					: null);
			final List<CloudDatumStreamMappingPropertyConfiguration> properties = (mapping != null
					? listCloudDatumStreamMappingProperties(restClient, objectMapper, mapping.configId())
					: null);

			List<Detail> details = (properties == null || properties.isEmpty()
					? List.of(new Detail(datumStream, mapping, integration, null))
					: properties.stream().map(p -> new Detail(datumStream, mapping, integration, p)).toList());

			List<?> tableData = (displayMode == ResultDisplayMode.JSON ? details
					: details.stream().map(c -> tableDataRow(c, false)).toList());
			TableUtils.renderTableData(tableDataColumns(), tableData, displayMode, objectMapper,
					TableUtils.TableDataJsonPrettyPrinter.INSTANCE, System.out);
			return 0;
		} catch (Exception e) {
			System.err.println("Error viewing cloud datum stream: %s".formatted(e.getMessage()));
		}
		return 1;
	}

	/**
	 * A detail record.
	 */
	public static record Detail(CloudDatumStreamConfiguration datumStream, CloudDatumStreamMappingConfiguration mapping,
			CloudIntegrationConfiguration integration, CloudDatumStreamMappingPropertyConfiguration property) {

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
	public static Object[] tableDataRow(Detail conf, boolean rawIdentifiers) {
		// @formatter:off
		return new Object[] {
				conf.datumStream.configId(),
				conf.datumStream.name(),
				(rawIdentifiers 
						? conf.datumStream.serviceIdentifier()
						: datumStreamServiceLocalizedName(conf.datumStream.serviceIdentifier())),
				conf.datumStream.enabled(),
				conf.datumStream.kind(),
				conf.datumStream.objectId(),
				sourceIdCellValue(conf.datumStream),
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

	private static String sourceIdCellValue(CloudDatumStreamConfiguration conf) {
		final var sprops = conf.serviceProperties();
		if (sprops != null && sprops.get("sourceIdMap") instanceof Map<?, ?> m) {
			return m.values().stream().map(Object::toString).sorted(CASE_INSENSITIVE_NATURAL_SORT)
					.collect(joining("\n"));
		}
		return conf.sourceId();
	}

	/**
	 * View a cloud datum stream.
	 * 
	 * @param restClient    the REST client
	 * @param objectMapper  the object mapper
	 * @param datumStreamId the datum stream ID to view
	 * @return the result
	 */
	public static CloudDatumStreamConfiguration viewCloudDatumStream(RestClient restClient, ObjectMapper objectMapper,
			Long datumStreamId) {
		// @formatter:off
		JsonNode response = restClient.get()
			.uri(b -> b.path("/solaruser/api/v1/sec/user/c2c/datum-streams/{datumStreamId}")
				.build(datumStreamId)
			)
			.accept(MediaType.APPLICATION_JSON)
			.retrieve()
			.body(JsonNode.class)
			;		
		// @formatter:on

		checkSuccess(response);

		try {
			return objectMapper.treeToValue(response.path("data"), CloudDatumStreamConfiguration.class);
		} catch (JsonProcessingException | IllegalArgumentException e) {
			throw new IllegalStateException("Error parsing cloud datum stream response: " + e.getMessage(), e);
		}
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
