package s10k.tool.datum.del.cmd;

import static com.github.freva.asciitable.HorizontalAlign.LEFT;
import static com.github.freva.asciitable.HorizontalAlign.RIGHT;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNullElse;
import static net.solarnetwork.util.ObjectUtils.nonnull;
import static s10k.tool.common.util.RestUtils.checkSuccess;
import static s10k.tool.common.util.StringUtils.parseLocalTimestamp;
import static s10k.tool.common.util.StringUtils.stringOrFileContents;
import static s10k.tool.common.util.TableUtils.tableConfig;

import java.io.InputStreamReader;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.jspecify.annotations.Nullable;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.freva.asciitable.Column;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import s10k.tool.common.cmd.BaseSubCmd;
import s10k.tool.common.domain.ResultDisplayMode;
import s10k.tool.common.util.SystemUtils;
import s10k.tool.common.util.TableUtils;
import s10k.tool.datum.domain.ObjectDatumId;

@Command(name = "ids", sortSynopsis = false, showDefaultValues = true, descriptionHeading = "%n", optionListHeading = "%n", description = {
		"Delete a small set of datum by identifiers.%n" })
public class DeleteDatumIdsCmd extends BaseSubCmd<DatumDeleteBaseCmd> implements Callable<Integer> {

	// @formatter:off
	@Option(names = { "-stream", "--stream-datum" },
			description = "a stream identifier to delete, in the form @|bold streamId:timestamp|@",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "streamDatumKey")
	String @Nullable [] streamDatumKeys;
	
	@Option(names = { "-node", "--node-datum" },
			description = "a node datum identifier to delete, in the form @|bold nodeId:sourceId:timestamp|@",
			split = "\\s*,\\s*",
			splitSynopsisLabel = ",",
			paramLabel = "nodeDatumKey")
	String @Nullable [] nodeDatumKeys;
	
	@Option(names = { "-tz", "--time-zone" },
			description = "a time zone to interpret the timestamps as, instead of the local time zone")
	@Nullable ZoneId zone;
	
	@Option(names = {"-I", "--ignore-input"},
			description = "do not try to read settings from standard input")
	public boolean ignoreStdIn;
	
	@Option(names = { "-mode", "--display-mode" },
			description = "how to display the data")
	@Nullable ResultDisplayMode displayMode;

	@Parameters(index = "0", paramLabel = "<identifiers>", description = "the JSON list of identifier objects to delete", arity = "0..1")
	@Nullable String value;
	// @formatter:on

	/**
	 * Constructor.
	 * 
	 * @param reqFactory   the HTTP request factory to use
	 * @param objectMapper the mapper to use
	 */
	public DeleteDatumIdsCmd(ClientHttpRequestFactory reqFactory, ObjectMapper objectMapper) {
		super(reqFactory, objectMapper);
	}

	@Override
	public Integer call() throws Exception {
		try {
			final RestClient restClient = restClient();
			final ObjectMapper objectMapper = objectMapper();
			final ResultDisplayMode displayMode = displayMode(this.displayMode);

			final SortedSet<ObjectDatumId> ids = new TreeSet<>();

			// look for JSON on stdin if allowed
			if (!(ignoreStdIn || SystemUtils.systemConsoleIsTerminal())) {
				ObjectDatumId[] idsArray = objectMapper.readValue(new InputStreamReader(System.in, UTF_8),
						ObjectDatumId[].class);
				if (idsArray != null) {
					for (ObjectDatumId id : idsArray) {
						ids.add(id);
					}
				}
			}

			populateIds(ids, requireNonNullElse(zone, zone()));

			if (value != null && !value.isBlank()) {
				ObjectDatumId[] idsArray = objectMapper.readValue(stringOrFileContents(value), ObjectDatumId[].class);
				if (idsArray != null) {
					for (ObjectDatumId id : idsArray) {
						ids.add(id);
					}
				}
			}

			final List<ObjectDatumId> result;
			if (isDryRun()) {
				result = List.copyOf(ids);
			} else {
				result = deleteDatumById(restClient, objectMapper, ids);
			}

			if (result.isEmpty()) {
				System.err.println("No datum were deleted.");
			}

			List<?> tableData = (displayMode == ResultDisplayMode.JSON ? result
					: result.stream().map(DeleteDatumIdsCmd::tableDataRow).toList());
			TableUtils.renderTableData(tableDataColumns(), tableData,
					tableConfig(this, displayMode, prettyStyle(), zone), objectMapper,
					TableUtils.TableDataJsonPrettyPrinter.INSTANCE, System.out);
			return 0;
		} catch (Exception e) {
			System.err.println("Error deleting datum by ID: %s".formatted(e.getMessage()));
		}
		return 1;
	}

	private void populateIds(Collection<ObjectDatumId> ids, ZoneId zone) {
		if (nodeDatumKeys != null) {
			for (String key : nodeDatumKeys) {
				final int firstDelim = key.indexOf(':');
				final int secondDelim = key.indexOf(':', firstDelim + 1);
				if (firstDelim > 0 && secondDelim > (firstDelim + 1) && secondDelim < key.length() - 1) {
					ObjectDatumId id = ObjectDatumId.nodeId(null, Long.valueOf(key.substring(0, firstDelim)),
							key.substring(firstDelim + 1, secondDelim),
							nonnull(parseLocalTimestamp(key.substring(secondDelim + 1), zone), "Timestamp"), null);
					ids.add(id);
				}
			}
		}
		if (streamDatumKeys != null) {
			for (String key : streamDatumKeys) {
				final int delim = key.indexOf(':');
				if (delim > 0 && delim < key.length() - 1) {
					ObjectDatumId id = ObjectDatumId.nodeId(UUID.fromString(key.substring(0, delim)), null, null,
							nonnull(parseLocalTimestamp(key.substring(delim + 1), zone), "Timestamp"), null);
					ids.add(id);
				}
			}
		}
	}

	/**
	 * Delete a set of datum identifiers.
	 * 
	 * @param restClient   the REST client
	 * @param objectMapper the object mapper
	 * @param ids          the identifiers to delete
	 * @return the updated job info
	 * @throws IllegalStateException if an error occurs
	 */
	public static List<ObjectDatumId> deleteDatumById(RestClient restClient, ObjectMapper objectMapper,
			Collection<ObjectDatumId> ids) {
		// @formatter:off
		final JsonNode response = checkSuccess(restClient.post()
			.uri("solaruser/api/v1/sec/user/expire/datum-delete/ids")
			.contentType(MediaType.APPLICATION_JSON)
			.body(ids)
			.accept(MediaType.APPLICATION_JSON)
			.retrieve()
			.body(JsonNode.class)
			);		
		// @formatter:on

		try {
			ObjectDatumId[] result = objectMapper.treeToValue(response.path("data"), ObjectDatumId[].class);
			return (result != null ? List.of(result).stream().sorted().toList() : List.of());
		} catch (JsonProcessingException | IllegalArgumentException e) {
			throw new IllegalStateException("Error parsing datum delete IDs response: " + e.getMessage(), e);
		}
	}

	/**
	 * Get datum delete job tabular structure columns.
	 * 
	 * @return the columns
	 * @see #tableDataRow(ObjectDatumId)
	 */
	public static Column[] tableDataColumns() {
		// @formatter:off
		return new Column[] {
				new Column().header("Kind").dataAlign(LEFT),
				new Column().header("Stream ID").dataAlign(LEFT),
				new Column().header("Object ID").dataAlign(RIGHT),
				new Column().header("Source ID").dataAlign(LEFT),
				new Column().header("Timestamp").dataAlign(LEFT),
			};
		// @formatter:on
	}

	/**
	 * Convert a datum identifier into a tabular structure.
	 * 
	 * @param id the identifier to convert
	 * @return the row data
	 * @see #tableDataColumns()
	 */
	public static Object[] tableDataRow(ObjectDatumId id) {
		// @formatter:off
		return new Object[] {
				id.getKind(),
				id.getStreamId(),
				id.getObjectId(),
				id.getSourceId(),
				id.getTimestamp(),
			};
		// @formatter:on
	}

}
