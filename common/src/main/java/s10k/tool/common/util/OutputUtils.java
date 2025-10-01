package s10k.tool.common.util;

import static org.springframework.util.StreamUtils.nonClosing;
import static s10k.tool.common.util.SystemUtils.systemConsoleIsTerminal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

	/**
	 * Ensure a directory exists.
	 * 
	 * @param outputDirectory the directory path, or {@code null} to use the current
	 *                        working directory
	 * @return the directory {@code Path}, or {@code null} if unable to create or
	 *         exists already but is not a directory
	 */
	public static Path ensureDirectory(String outputDirectory) {
		final Path outputDir = Paths.get(outputDirectory != null ? outputDirectory : ".");
		if (!Files.exists(outputDir)) {
			try {
				Files.createDirectories(outputDir);
			} catch (Exception e) {
				System.err.println("Error creating output directory [%s]: %s".formatted(outputDir, e.getMessage()));
				return null;
			}
		} else if (!Files.isDirectory(outputDir)) {
			System.err.println("[%s] is not a directory.".formatted(outputDir));
			return null;
		}
		return outputDir;
	}

}
