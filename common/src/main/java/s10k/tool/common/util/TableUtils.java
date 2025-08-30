package s10k.tool.common.util;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.supercsv.prefs.CsvPreference.STANDARD_PREFERENCE;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SequencedCollection;

import org.supercsv.io.CsvListWriter;
import org.supercsv.io.ICsvListWriter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.freva.asciitable.AsciiTable;

import s10k.tool.common.domain.TableDisplayMode;

/**
 * Helper methods for generating formatted tables.
 */
public class TableUtils {

	/**
	 * Generate a basic ASCII table structure for a map.
	 *
	 * @param map               the map
	 * @param keyName           the key header column name
	 * @param valName           the value header column name
	 * @param valRightJustified {@code true} to right-justify values
	 * @return the formatted table
	 */
	public static String basicTable(Map<?, ?> map, String keyName, String valName, boolean valRightJustified) {
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
	 * Render tabular data to an output stream.
	 * 
	 * @param data         the data to render
	 * @param mode         the output mode
	 * @param objectMapper the JSON mapper to use (only required for if {@code mode}
	 *                     is {@code JSON})
	 * @param out          the output stream
	 * @throws IOException if any IO error occurs
	 */
	public static void renderTableData(SequencedCollection<? extends SequencedCollection<?>> data,
			TableDisplayMode mode, ObjectMapper objectMapper, OutputStream out) throws IOException {
		if (data == null || data.isEmpty()) {
			return;
		}
		if (mode == TableDisplayMode.CSV) {
			try (ICsvListWriter csvWriter = new CsvListWriter(new OutputStreamWriter(out, UTF_8),
					STANDARD_PREFERENCE)) {
				for (SequencedCollection<?> row : data) {
					csvWriter.write(row.toArray(Object[]::new));
				}
			}
		} else if (mode == TableDisplayMode.JSON) {
			objectMapper.writer(TableDataJsonPrettyPrinter.INSTANCE).writeValue(out, data);
			out.write(System.lineSeparator().getBytes(Charset.defaultCharset()));
		} else {
			// @formatter:off
			AsciiTable.builder()
				.data(data.stream().map(l -> l.toArray(Object[]::new)).toArray(Object[][]::new))
				.writeTo(out);
				;
			out.write(System.lineSeparator().getBytes(Charset.defaultCharset()));
			// @formatter:on			
		}
	}

}
