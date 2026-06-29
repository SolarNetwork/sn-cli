package s10k.tool.c2c.ds.cmd;

import static com.github.freva.asciitable.HorizontalAlign.LEFT;
import static com.github.freva.asciitable.HorizontalAlign.RIGHT;
import static s10k.tool.c2c.util.CloudIntegrationRestUtils.listCloudDatumStreams;
import static s10k.tool.c2c.util.CloudIntegrationsUtils.datumStreamServiceLocalizedName;

import java.util.List;
import java.util.concurrent.Callable;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.freva.asciitable.Column;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import s10k.tool.c2c.domain.CloudDatumStreamConfiguration;
import s10k.tool.c2c.domain.CloudIntegrationsFilter;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.util.TableUtils;

/**
 * List Cloud Datum Stream configurations.
 */
@Component
@Command(name = "list", sortSynopsis = false)
public class ListDatumStreamsCmd extends BaseSubCmd<DatumStreamsCmd> implements Callable<Integer> {

	// @formatter:off
	@Option(names = { "-stream", "--stream-id" },
			description = "a datum stream ID to match",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "datumStreamId")
	Long[] datumStreamIds;

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
	public ListDatumStreamsCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		final RestClient restClient = restClient();
		final CloudIntegrationsFilter filter = filter();
		try {
			List<CloudDatumStreamConfiguration> confs = listCloudDatumStreams(restClient, objectMapper, filter);
			if (confs.isEmpty()) {
				System.err.println("No datum streams matched your criteria.");
				return 0;
			}

			List<?> tableData = (displayMode == ResultDisplayMode.JSON ? confs
					: confs.stream().map(c -> tableDataRow(c, false)).toList());
			TableUtils.renderTableData(tableDataColumns(), tableData, displayMode, objectMapper,
					TableUtils.TableDataJsonPrettyPrinter.INSTANCE, System.out);
			return 0;
		} catch (Exception e) {
			System.err.println("Error viewing cloud datum streams: %s".formatted(e.getMessage()));
		}
		return 1;
	}

	private CloudIntegrationsFilter filter() {
		final CloudIntegrationsFilter filter = new CloudIntegrationsFilter();
		if (datumStreamIds != null && datumStreamIds.length > 0) {
			filter.setDatumStreamIds(List.of(datumStreamIds));
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
				new Column().header("Kind").dataAlign(LEFT),
				new Column().header("Object ID").dataAlign(RIGHT),
				new Column().header("Source ID").dataAlign(LEFT),
				new Column().header("Schedule").dataAlign(LEFT),
				new Column().header("Mapping ID").dataAlign(RIGHT),
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
	public static Object[] tableDataRow(CloudDatumStreamConfiguration conf, boolean rawIdentifiers) {
		// @formatter:off
		return new Object[] {
				conf.configId(),
				conf.name(),
				(rawIdentifiers ? conf.serviceIdentifier() : datumStreamServiceLocalizedName(conf.serviceIdentifier())),
				conf.enabled(),
				conf.kind(),
				conf.objectId(),
				conf.sourceIdsValue(),
				conf.schedule(),
				conf.datumStreamMappingId(),
			};
		// @formatter:on
	}

}
