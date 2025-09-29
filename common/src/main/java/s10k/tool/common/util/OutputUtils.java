package s10k.tool.common.util;

import static org.springframework.util.StreamUtils.nonClosing;
import static s10k.tool.common.util.SystemUtils.systemConsoleIsTerminal;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Output helper methods.
 */
public final class OutputUtils {

	private OutputUtils() {
		// not available
	}

	/**
	 * Write a single object as JSON to standard out.
	 * 
	 * @param objectMapper the object mapper to use
	 * @param result       the object to write
	 * @throws IOException if any IO error occurs
	 */
	public static void writeJsonObject(ObjectMapper objectMapper, Object result) throws IOException {
		(systemConsoleIsTerminal() ? objectMapper.writerWithDefaultPrettyPrinter() : objectMapper.writer())
				.writeValue(nonClosing(System.out), result);
		if (systemConsoleIsTerminal()) {
			System.out.println();
		}
	}
}
