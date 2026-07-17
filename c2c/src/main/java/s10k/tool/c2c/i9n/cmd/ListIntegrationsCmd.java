package s10k.tool.c2c.i9n.cmd;

import static com.github.freva.asciitable.HorizontalAlign.LEFT;
import static com.github.freva.asciitable.HorizontalAlign.RIGHT;
import static s10k.tool.c2c.util.CloudIntegrationRestUtils.listCloudIntegrations;
import static s10k.tool.c2c.util.CloudIntegrationsUtils.integrationServiceLocalizedName;

import java.util.List;
import java.util.concurrent.Callable;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.freva.asciitable.Column;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import s10k.tool.c2c.domain.CloudIntegrationConfiguration;
import s10k.tool.c2c.domain.CloudIntegrationsFilter;
import s10k.tool.c2c.domain.EnabledOrDisabled;
import s10k.tool.c2c.util.CloudIntegrationsUtils;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.util.TableUtils;

/**
 * View Cloud Integration configurations.
 */
@Component
@Command(name = "list", sortSynopsis = false)
public class ListIntegrationsCmd extends BaseSubCmd<IntegrationsCmd> implements Callable<Integer> {

	// @formatter:off
	@ArgGroup(exclusive = true, multiplicity = "0..1")
	EnabledOrDisabled enabledOrDisabled;

	@Option(names = { "-i", "--integration-id" },
			description = "an integration ID to match",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "integrationId")
	Long[] integrationIds;

	@Option(names = { "-stream", "--stream-id" },
			description = "a datum stream ID to match",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "datumStreamId")
	Long[] datumStreamIds;

	@Option(names = { "-map", "--mapping-id" },
			description = "an datum stream mapping ID to match",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "mappingId")
	Long[] mappingIds;

	@Option(names = { "-m", "--name" },
			description = "a name to match",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "name")
	String[] names;
	
	@Option(names = { "-S", "--service" },
			description = "a name to match",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "serviceIdent")
	String[] serviceIdentifiers;
	
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
	public ListIntegrationsCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		final RestClient restClient = restClient();
		final CloudIntegrationsFilter filter = filter();

		try {
			List<CloudIntegrationConfiguration> confs = listCloudIntegrations(restClient, objectMapper, filter);
			if (confs.isEmpty()) {
				System.err.println("No sources matched your criteria.");
				return 0;
			}

			List<?> tableData = (displayMode == ResultDisplayMode.JSON ? confs
					: confs.stream().map(c -> tableDataRow(c, false)).toList());
			TableUtils.renderTableData(tableDataColumns(), tableData, displayMode, objectMapper,
					TableUtils.TableDataJsonPrettyPrinter.INSTANCE, System.out);
			return 0;
		} catch (Exception e) {
			System.err.println("Error viewing cloud integrations: %s".formatted(e.getMessage()));
		}
		return 1;
	}

	private CloudIntegrationsFilter filter() {
		final CloudIntegrationsFilter filter = new CloudIntegrationsFilter();
		if (enabledOrDisabled != null) {
			filter.setEnabled(enabledOrDisabled.enabled);
		}
		if (integrationIds != null && integrationIds.length > 0) {
			filter.setIntegrationIds(List.of(integrationIds));
		}
		if (mappingIds != null && mappingIds.length > 0) {
			filter.setDatumStreamMappingIds(List.of(mappingIds));
		}
		if (datumStreamIds != null && datumStreamIds.length > 0) {
			filter.setDatumStreamIds(List.of(datumStreamIds));
		}
		if (names != null && names.length > 0) {
			filter.setNames(List.of(names));
		}
		if (serviceIdentifiers != null && serviceIdentifiers.length > 0) {
			filter.setServiceIdentifiers(CloudIntegrationsUtils.findIntegrationServiceIds(serviceIdentifiers));
		}
		return filter;
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
	public static Object[] tableDataRow(CloudIntegrationConfiguration conf, boolean rawIdentifiers) {
		// @formatter:off
		return new Object[] {
				conf.configId(),
				conf.name(),
				(rawIdentifiers ? conf.serviceIdentifier() : integrationServiceLocalizedName(conf.serviceIdentifier())),
				conf.enabled(),
			};
		// @formatter:on
	}

}
