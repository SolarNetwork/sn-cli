package s10k.tool.common.util;

import static com.github.freva.asciitable.HorizontalAlign.LEFT;
import static com.github.freva.asciitable.HorizontalAlign.RIGHT;
import static net.solarnetwork.util.DateUtils.ISO_DATE_OPT_TIME_ALT_LOCAL;
import static org.springframework.util.StreamUtils.nonClosing;
import static s10k.tool.common.util.DateUtils.local;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SequencedCollection;

import org.jspecify.annotations.Nullable;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.AsciiTableBuilder;
import com.github.freva.asciitable.Column;

import de.siegmar.fastcsv.writer.CsvWriter;
import net.solarnetwork.util.StringUtils;
import s10k.tool.common.domain.PrettyStyle;
import s10k.tool.common.domain.ProfileProvider;
import s10k.tool.common.domain.ResultDisplayMode;

/**
 * Helper methods for generating formatted tables.
 */
public class TableUtils {

	/**
	 * Create a column header for map entries.
	 * 
	 * @param keyName           the key column name
	 * @param valueName         the value column name
	 * @param valRightJustified {@code true} to right-justify the value column, or
	 *                          {@code false} for left justification
	 * @return the columns
	 */
	public static Column[] mapColumns(String keyName, String valueName, boolean valRightJustified) {
		// @formatter:off
		return new Column[] {
				new Column().header(keyName).dataAlign(LEFT),
				new Column().header(valueName).dataAlign(valRightJustified ? RIGHT : LEFT),
			};
		// @formatter:on
	}

	/**
	 * Generate a basic ASCII table structure for a map.
	 * 
	 * <p>
	 * The resulting string is a simple formatted key/value listing of the map
	 * entries. The keys are left-justified and given enough space to accommodate
	 * the longest value so the values align vertically when left-justified. The
	 * values can instead be right-justified if {@code valRightJustified} is
	 * {@code true}.
	 * </p>
	 * 
	 * <p>
	 * For example, given a Map {@code m} of
	 * {@code {"max":123, "mode":"advanced", "repeat":true}} then calling this
	 * method like {@code basicTable(m, "Property", "Value", false)} would output:
	 * </p>
	 * 
	 * <pre>{@code
	 * Property     Value
	 * ---------------------
	 * max          123
	 * mode         advanced
	 * repeatAlways true
	 * }</pre>
	 *
	 * @param map               the map
	 * @param keyName           the key header column name
	 * @param valName           the value header column name
	 * @param valRightJustified {@code true} to right-justify values
	 * @return the formatted table
	 */
	public static @Nullable String basicTable(@Nullable Map<?, ?> map, @Nullable String keyName,
			@Nullable String valName, boolean valRightJustified) {
		if (map == null || map.isEmpty()) {
			return null;
		}
		int keyWidth = keyName != null ? keyName.length() : 0;
		int valWidth = valName != null ? valName.length() : 0;
		final Map<String, Object> dispMap = new LinkedHashMap<>(map.size());
		for (Map.Entry<?, ?> e : map.entrySet()) {
			String k = e.getKey().toString();
			String v = e.getValue() != null ? e.getValue().toString() : "";

			keyWidth = Math.max(keyWidth, k.length());

			// look for newline split
			if (v.indexOf('\n') > 0) {
				String[] lines = v.split("\\n", 0);
				for (String line : lines) {
					valWidth = Math.max(valWidth, line.length());
				}
				dispMap.put(k, lines);
			} else {
				valWidth = Math.max(valWidth, v.length());
				dispMap.put(k, v);
			}
		}

		keyWidth = Math.min(keyWidth, 28);
		valWidth = Math.min(valWidth, 68);

		final String tmpl = "%-" + keyWidth + "s %" + (valRightJustified ? "" : "-") + valWidth + "s\n";
		StringBuilder buf = new StringBuilder(256);
		if (keyName != null && valName != null && !keyName.isBlank() && !valName.isBlank()) {
			buf.append(String.format(tmpl, keyName, valName));
			for (int i = 0, len = keyWidth + 1 + valWidth; i < len; i++) {
				buf.append('-');
			}
			buf.append('\n');
		}
		for (Map.Entry<String, Object> e : dispMap.entrySet()) {
			Object v = e.getValue();
			if (v instanceof String[] lines) {
				for (int i = 0, len = lines.length; i < len; i++) {
					if (i == 0) {
						buf.append(String.format(tmpl, e.getKey(), lines[i]));
					} else {
						buf.append(String.format(tmpl, "", lines[i]));
					}
				}
			} else {
				buf.append(String.format(tmpl, e.getKey(), e.getValue()));
			}
		}
		return buf.toString();
	}

	/**
	 * Custom {@link PrettyPrinter} to print table data with rows on individual
	 * lines.
	 */
	public static final class TableDataJsonPrettyPrinter extends DefaultPrettyPrinter {

		private static final long serialVersionUID = -6875910774013342642L;

		/** A default instance. */
		public static final TableDataJsonPrettyPrinter INSTANCE = new TableDataJsonPrettyPrinter();

		private int arrayNestingLevel = 0;

		public TableDataJsonPrettyPrinter() {
			super();
			// indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
		}

		@Override
		public DefaultPrettyPrinter createInstance() {
			return new TableDataJsonPrettyPrinter();
		}

		@Override
		public void beforeArrayValues(JsonGenerator g) throws IOException {
			if (arrayNestingLevel > 1) {
				super.beforeArrayValues(g);
			}
		}

		@Override
		public void writeStartArray(JsonGenerator g) throws IOException {
			arrayNestingLevel++;
			if (arrayNestingLevel == 1) {
				g.writeRaw('[');
				g.writeRaw(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE.getEol());
			} else {
				if (arrayNestingLevel == 2) {
					g.writeRaw("  ");
				}
				super.writeStartArray(g);
			}
		}

		@Override
		public void writeEndArray(JsonGenerator g, int nrOfValues) throws IOException {
			arrayNestingLevel--;
			if (arrayNestingLevel == 0) {
				g.writeRaw(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE.getEol());
				g.writeRaw(']');
			} else {
				super.writeEndArray(g, nrOfValues);
			}
		}

		@Override
		public void writeArrayValueSeparator(JsonGenerator g) throws IOException {
			if (arrayNestingLevel == 1) {
				g.writeRaw(',');
				g.writeRaw(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE.getEol());
			} else {
				super.writeArrayValueSeparator(g);
			}
		}
	}

	/**
	 * A tabular display configuration.
	 */
	public record TableConfiguration(ResultDisplayMode mode, PrettyStyle prettyStyle, ZoneId zone,
			boolean singletonJsonObject) {

		/**
		 * Create a copy with "singleton JSON mode" enabled.
		 * 
		 * @return the copy
		 */
		public TableConfiguration asJsonSingleton() {
			return new TableConfiguration(mode, prettyStyle, zone, true);
		}

	}

	/**
	 * Resolve a table configuration.
	 * 
	 * @param profileProvider the profile provider for configuration
	 * @param mode            the specifically desired display mode
	 * @param prettyStyle     the specifically desired pretty style
	 * @return the configuration
	 */
	public static TableConfiguration tableConfig(final @Nullable ProfileProvider profileProvider,
			final @Nullable ResultDisplayMode mode, @Nullable PrettyStyle prettyStyle) {
		return tableConfig(profileProvider, mode, prettyStyle, null);
	}

	/**
	 * Resolve a table configuration.
	 * 
	 * @param profileProvider the profile provider for configuration
	 * @param mode            the specifically desired display mode
	 * @param prettyStyle     the specifically desired pretty style
	 * @param zone            the specifically desired display time zone
	 * @return the configuration
	 */
	public static TableConfiguration tableConfig(final @Nullable ProfileProvider profileProvider,
			final @Nullable ResultDisplayMode mode, @Nullable PrettyStyle prettyStyle, final @Nullable ZoneId zone) {
		ResultDisplayMode dispMode = mode;
		PrettyStyle style = prettyStyle;
		ZoneId tz = zone;
		if (profileProvider != null) {
			dispMode = profileProvider.displayMode();
			style = profileProvider.prettyStyle();
			tz = profileProvider.zone();
		}
		if (mode != null) {
			dispMode = mode;
		} else if (dispMode == null) {
			dispMode = ResultDisplayMode.PRETTY;
		}
		if (prettyStyle != null) {
			style = prettyStyle;
		} else if (style == null) {
			style = PrettyStyle.DEFAULT_PRETTY_STYLE;
		}
		if (zone != null) {
			tz = zone;
		} else if (tz == null) {
			tz = ZoneId.systemDefault();
		}
		return new TableConfiguration(dispMode, style, tz, false);
	}

	/**
	 * Render tabular data to an output stream.
	 * 
	 * @param data         the data to render; the collection value type can be
	 *                     {@code Object[]} or {@code Collection<?>} or
	 *                     {@code Object}; or if the collection holds a single
	 *                     {@code Map} value, the map entries will be enumerated
	 *                     into a 2-column table, or written directly in
	 *                     {@code JSON} mode
	 * @param config       the table configuration
	 * @param objectMapper the JSON mapper to use (only required for if {@code mode}
	 *                     is {@code JSON})
	 * @param out          the output stream
	 * @throws IOException if any IO error occurs
	 */
	public static void renderTableData(@Nullable SequencedCollection<?> data, TableConfiguration config,
			ObjectMapper objectMapper, OutputStream out) throws IOException {
		renderTableData(null, data, config, objectMapper, null, out);
	}

	/**
	 * Render tabular data to an output stream.
	 * 
	 * @param columns      optional column information; ignored for JSON output
	 * @param data         the data to render; the collection value type can be
	 *                     {@code Object[]} or {@code Collection<?>} or
	 *                     {@code Object}; or if the collection holds a single
	 *                     {@code Map} value, the map entries will be enumerated
	 *                     into a 2-column table, or written directly in
	 *                     {@code JSON} mode
	 * @param config       the table configuration
	 * @param objectMapper the JSON mapper to use (only required for if {@code mode}
	 *                     is {@code JSON})
	 * @param out          the output stream
	 * @throws IOException if any IO error occurs
	 */
	public static void renderTableData(Column @Nullable [] columns, @Nullable SequencedCollection<?> data,
			TableConfiguration config, ObjectMapper objectMapper, OutputStream out) throws IOException {
		renderTableData(columns, data, config, objectMapper, null, out);
	}

	/**
	 * Render tabular data to an output stream.
	 * 
	 * @param columns             optional column information; ignored for JSON
	 *                            output
	 * @param data                the data to render; the collection value type can
	 *                            be {@code Object[]} or {@code Collection<?>} or
	 *                            {@code Object}; or if the collection holds a
	 *                            single {@code Map} value, the map entries will be
	 *                            enumerated into a 2-column table, or written
	 *                            directly in {@code JSON} mode
	 * @param config              the table configuration
	 * @param objectMapper        the JSON mapper to use (only required for if
	 *                            {@code mode} is {@code JSON})
	 * @param customJsonFormatter an optional formatter to use, or {@code null} for
	 *                            default
	 * @param out                 the output stream
	 * @throws IOException if any IO error occurs
	 */
	public static void renderTableData(Column @Nullable [] columns, @Nullable SequencedCollection<?> data,
			TableConfiguration config, @Nullable ObjectMapper objectMapper, @Nullable PrettyPrinter customJsonFormatter,
			OutputStream out) throws IOException {
		if (data == null || data.isEmpty()) {
			return;
		}

		final ResultDisplayMode mode = config.mode();
		final ZoneId zone = config.zone();

		Map<?, ?> mapData = null;
		if (data.size() == 1 && data.getFirst() instanceof Map<?, ?> m) {
			mapData = m;
		}
		if (mode == ResultDisplayMode.CSV) {
			try (CsvWriter csv = CsvWriter.builder().build(nonClosing(out))) {
				if (columns != null) {
					csv.writeRecord(Arrays.stream(columns).map(Column::getHeader).toArray(String[]::new));
				}
				if (mapData != null) {
					for (Entry<?, ?> e : mapData.entrySet()) {
						csv.writeRecord(optionalStringValue(e.getKey()), cellValue(e.getValue(), zone));
					}
				} else {
					for (Object row : data) {
						if (row instanceof String[] a) {
							csv.writeRecord(Arrays.stream(a).map(e -> cellValue(e, zone)).toArray(String[]::new));
						} else if (row instanceof Object[] a) {
							csv.writeRecord(Arrays.stream(a).map(e -> cellValue(e, zone)).toArray(String[]::new));
						} else if (row instanceof Collection<?> l) {
							csv.writeRecord(l.stream().map(e -> cellValue(e, zone)).toArray(String[]::new));
						} else {
							csv.writeRecord(cellValue(row, zone));
						}
					}
				}
			}
		} else if (mode == ResultDisplayMode.JSON && objectMapper != null) {
			final Object jsonData = (mapData != null ? mapData
					: config.singletonJsonObject() ? (!data.isEmpty() ? data.getFirst() : null) : data);
			if (jsonData != null) {
				if (SystemUtils.systemConsoleIsTerminal()) {
					if (customJsonFormatter != null) {
						objectMapper.writer(TableDataJsonPrettyPrinter.INSTANCE).writeValue(nonClosing(out), jsonData);
					} else {
						objectMapper.writerWithDefaultPrettyPrinter().writeValue(nonClosing(System.out), jsonData);
					}
					out.write(System.lineSeparator().getBytes(Charset.defaultCharset()));
				} else {
					objectMapper.writeValue(nonClosing(System.out), jsonData);
				}
			}
		} else {
			@Nullable
			Object[][] tableData;
			if (mapData != null) {
				tableData = (@Nullable Object[][]) mapData.entrySet().stream()
						.map(e -> new Object[] { e.getKey(), cellValue(e.getValue(), zone) }).toArray(Object[][]::new);
			} else {
				tableData = (@Nullable Object[][]) data.stream().map(row -> {
					if (row instanceof Object[] a) {
						return a;
					} else if (row instanceof Collection<?> l) {
						return l.toArray(Object[]::new);
					}
					return new Object[] { row };
				}).map(row -> {
					if (row == null) {
						return row;
					}
					@Nullable
					String[] cells = new String[row.length];
					for (int i = 0; i < cells.length; i++) {
						cells[i] = cellValue(row[i], zone);
					}
					return cells;
				}).toArray(Object[][]::new);
			}
			AsciiTableBuilder atb = AsciiTable.builder();
			atb.border(switch (config.prettyStyle()) {
			case Fancy -> AsciiTable.FANCY_ASCII;
			default -> AsciiTable.BASIC_ASCII;
			});
			if (columns != null) {
				atb.data(columns, tableData);
			} else {
				atb.data(tableData);
			}
			atb.writeTo(out);
			out.write(System.lineSeparator().getBytes(Charset.defaultCharset()));
		}
	}

	private static @Nullable String optionalStringValue(@Nullable Object v) {
		return (v != null ? v.toString() : null);
	}

	private static @Nullable String cellValue(@Nullable Object val, ZoneId zone) {
		if (val instanceof Collection<?> c) {
			val = StringUtils.commaDelimitedStringFromCollection(c);
		} else if (val instanceof Map<?, ?> m) {
			val = StringUtils.delimitedStringFromMap(m);
		} else if (val instanceof Instant date) {
			ZoneOffset offset = (zone instanceof ZoneOffset o ? o : zone.getRules().getOffset(date));
			val = ISO_DATE_OPT_TIME_ALT_LOCAL.format(local(date, zone)) + offset;
		} else if (val instanceof LocalDateTime date) {
			val = ISO_DATE_OPT_TIME_ALT_LOCAL.format(date);
		} else if (val instanceof ZonedDateTime date) {
			ZoneOffset offset = (zone instanceof ZoneOffset o ? o : zone.getRules().getOffset(date.toInstant()));
			val = ISO_DATE_OPT_TIME_ALT_LOCAL.format(date.withZoneSameInstant(zone).toLocalDateTime()) + offset;
		}
		return optionalStringValue(val);
	}
}
