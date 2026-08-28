package s10k.tool.c2c.mapping;

import static com.github.freva.asciitable.HorizontalAlign.LEFT;
import static com.github.freva.asciitable.HorizontalAlign.RIGHT;
import static net.solarnetwork.util.ObjectUtils.nonnull;
import static s10k.tool.c2c.util.CloudIntegrationRestUtils.listCloudDatumStreamMappings;
import static s10k.tool.common.util.TableUtils.tableConfig;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import org.jspecify.annotations.Nullable;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.freva.asciitable.Column;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import s10k.tool.c2c.domain.CloudDatumStreamMappingConfiguration;
import s10k.tool.c2c.domain.CloudDatumStreamMappingInfo;
import s10k.tool.c2c.domain.CloudDatumStreamMappingPropertyConfiguration;
import s10k.tool.c2c.domain.CloudDatumStreamMappingPropertyInfo;
import s10k.tool.c2c.domain.CloudIntegrationConfiguration;
import s10k.tool.c2c.domain.CloudIntegrationsFilter;
import s10k.tool.c2c.util.CloudIntegrationRestUtils;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.PrettyStyle;
import s10k.tool.common.domain.ProfileProvider;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.util.OutputUtils;
import s10k.tool.common.util.TableUtils;

/**
 * List Cloud Datum Stream Mapping configurations.
 */
@Command(name = "list", sortSynopsis = false, showDefaultValues = true, descriptionHeading = "%n", optionListHeading = "%n", description = {
		// @formatter:off
		"List cloud datum stream mapping configurations matching search criteria.%n", 
		// @formatter:on
})
public class ListMappingsCmd extends BaseSubCmd<MappingsCmd> implements Callable<Integer> {

	// @formatter:off
	@Option(names = { "-i", "--integration-id" },
			description = "an integration ID to match",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "integrationId")
	Long @Nullable [] integrationIds;
	
	@Option(names = { "-map", "--mapping-id" },
			description = "a datum stream mapping ID to match",
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
	public ListMappingsCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		try {
			final RestClient restClient = restClient();
			final ObjectMapper objectMapper = objectMapper();
			final ResultDisplayMode displayMode = displayMode(this.displayMode);
			final CloudIntegrationsFilter filter = filter();

			final List<CloudDatumStreamMappingConfiguration> confs = listCloudDatumStreamMappings(restClient,
					objectMapper, filter);
			if (confs.isEmpty()) {
				System.err.println("No mappings matched your criteria.");
				return 0;
			}

			renderMappings(restClient, objectMapper, this, displayMode, prettyStyle(), confs);
		} catch (Exception e) {
			System.err.println("Error listing cloud datum stream mappings: %s".formatted(e.getMessage()));
		}
		return 1;
	}

	/**
	 * Render a list of mapping configurations.
	 * 
	 * @param restClient      the REST client
	 * @param objectMapper    the object mapper
	 * @param profileProvider the profile provider
	 * @param displayMode     the display mode
	 * @param prettyStyle     the pretty style
	 * @param confs           the configurations
	 * @throws IOException if any IO error occurs
	 */
	public static void renderMappings(final RestClient restClient, final ObjectMapper objectMapper,
			final @Nullable ProfileProvider profileProvider, final @Nullable ResultDisplayMode displayMode,
			@Nullable PrettyStyle prettyStyle, final List<CloudDatumStreamMappingConfiguration> confs)
			throws IOException {
		final Map<Long, CloudIntegrationConfiguration> integrations = new HashMap<>(confs.size());
		final Map<Long, List<CloudDatumStreamMappingPropertyConfiguration>> props = new HashMap<>(confs.size());
		for (CloudDatumStreamMappingConfiguration conf : confs) {
			final List<CloudDatumStreamMappingPropertyConfiguration> properties = CloudIntegrationRestUtils
					.listCloudDatumStreamMappingProperties(restClient, objectMapper, conf.configId());
			props.put(conf.configId(), properties);
			if (!integrations.containsKey(conf.integrationId())) {
				integrations.put(conf.integrationId(),
						CloudIntegrationRestUtils.viewCloudIntegration(restClient, objectMapper, conf.integrationId()));
			}
		}

		if (displayMode == ResultDisplayMode.JSON) {
			List<CloudDatumStreamMappingInfo> infos = new ArrayList<>(confs.size());
			for (CloudDatumStreamMappingConfiguration conf : confs) {
				infos.add(CloudDatumStreamMappingInfo.mappingPropertiesInfo(conf,
						nonnull(integrations.get(conf.integrationId()), "Integration"), props.get(conf.configId())));
			}
			OutputUtils.writeJsonObject(objectMapper, infos);
		} else {
			final List<CloudDatumStreamMappingPropertyInfo> infos = new ArrayList<>();
			for (CloudDatumStreamMappingConfiguration conf : confs) {
				CloudIntegrationConfiguration integration = nonnull(integrations.get(conf.integrationId()),
						"Integration");
				List<CloudDatumStreamMappingPropertyConfiguration> properties = props.get(conf.configId());
				if (properties == null || properties.isEmpty()) {
					infos.add(new CloudDatumStreamMappingPropertyInfo(conf, integration, null));
				} else {
					for (CloudDatumStreamMappingPropertyConfiguration prop : properties) {
						infos.add(new CloudDatumStreamMappingPropertyInfo(conf, integration, prop));
					}
				}
			}
			final Set<Long> lastSeenMapping = new HashSet<>(1);
			final List<?> tableData = infos.stream().map(c -> tableDataRow(c, lastSeenMapping)).toList();
			TableUtils.renderTableData(tableDataColumns(), tableData,
					tableConfig(profileProvider, displayMode, prettyStyle), objectMapper,
					TableUtils.TableDataJsonPrettyPrinter.INSTANCE, System.out);
		}
	}

	private CloudIntegrationsFilter filter() {
		final CloudIntegrationsFilter filter = new CloudIntegrationsFilter();
		if (integrationIds != null && integrationIds.length > 0) {
			filter.setIntegrationIds(List.of(integrationIds));
		}
		if (mappingIds != null && mappingIds.length > 0) {
			filter.setDatumStreamMappingIds(List.of(mappingIds));
		}
		return filter;
	}

	/**
	 * Get datum stream mapping info tabular structure columns.
	 * 
	 * @return the columns
	 */
	public static Column[] tableDataColumns() {
		// @formatter:off
		return new Column[] {
				new Column().header("ID").dataAlign(RIGHT),
				new Column().header("Name").dataAlign(LEFT),
				new Column().header("Integration ID").dataAlign(RIGHT),
				new Column().header("Integration Name").dataAlign(LEFT),
				new Column().header("Integration Enabled").dataAlign(LEFT),
				new Column().header("Property #").dataAlign(RIGHT),
				new Column().header("Property Enabled").dataAlign(LEFT),
				new Column().header("Property Type").dataAlign(LEFT),
				new Column().header("Property Name").dataAlign(LEFT),
				new Column().header("Value Type").dataAlign(LEFT),
				new Column().header("Value Reference").dataAlign(LEFT),
				new Column().header("Multiplier").dataAlign(RIGHT),
				new Column().header("Scale").dataAlign(RIGHT),
			};
		// @formatter:on
	}

	/**
	 * Convert mapping property info listing into a tabular structure.
	 * 
	 * @param conf          the configuration to convert
	 * @param lastMappingId a mutable set that holds the ID of the last-processed
	 *                      mapping, for tracking the display of non-property
	 *                      details
	 * @return the metadata data
	 */
	public static Object[] tableDataRow(CloudDatumStreamMappingPropertyInfo conf, Set<Long> lastMappingId) {
		boolean newMapping = (lastMappingId.isEmpty()
				|| (conf.mapping() != null && !lastMappingId.contains(conf.mapping().configId())));
		if (conf.mapping() != null) {
			lastMappingId.add(conf.mapping().configId());
		}
		// @formatter:off
		return new Object[] {
				(newMapping ? conf.mapping().configId() : null),
				(newMapping ? conf.mapping().name() : null),
				(newMapping ? conf.mapping().integrationId() : null),
				(newMapping ? conf.integration().name() : null),
				(newMapping ? conf.integration().enabled() : null),
				(conf.property() != null ? conf.property().index() : null),
				(conf.property() != null ? conf.property().enabled() : null),
				(conf.property() != null ? conf.property().propertyType() : null),
				(conf.property() != null ? conf.property().propertyName() : null),
				(conf.property() != null ? conf.property().valueType() : null),
				(conf.property() != null ? conf.property().valueReference() : null),
				(conf.property() != null ? conf.property().multiplier() : null),
				(conf.property() != null ? conf.property().scale() : null),
			};
		// @formatter:on
	}

}
