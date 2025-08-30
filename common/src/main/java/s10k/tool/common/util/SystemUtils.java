package s10k.tool.common.util;

import java.io.Console;

/**
 * System helper methods.
 */
public final class SystemUtils {

	private SystemUtils() {
		// not available
	}

	/**
	 * Test if the system is connected to a terminal.
	 * 
	 * @return {@code true} if the system is connected to a terminal
	 * @see <a href=
	 *      "https://errorprone.info/bugpattern/SystemConsoleNull">SystemConsoleNull</a>
	 */
	@SuppressWarnings("SystemConsoleNull")
	public static boolean systemConsoleIsTerminal() {
		Console systemConsole = System.console();
		if (Runtime.version().feature() < 22 || systemConsole == null) {
			return systemConsole != null;
		}
		try {
			return (Boolean) Console.class.getMethod("isTerminal").invoke(systemConsole);
		} catch (ReflectiveOperationException e) {
			throw new LinkageError(e.getMessage(), e);
		}
	}

}
