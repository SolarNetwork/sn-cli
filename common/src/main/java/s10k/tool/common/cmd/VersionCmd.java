package s10k.tool.common.cmd;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.Callable;

import picocli.CommandLine.Command;
import picocli.CommandLine.IVersionProvider;

/**
 * Print out the application version.
 */
@Command(name = "version", sortSynopsis = false, showDefaultValues = true)
public class VersionCmd implements Callable<Integer>, IVersionProvider {

	/**
	 * Constructor.
	 */
	public VersionCmd() {
		super();
	}

	@Override
	public Integer call() throws Exception {
		System.out.println(getVersion()[0]);
		return 0;
	}

	@Override
	public String[] getVersion() throws Exception {
		final var props = new Properties();
		String version = null;
		try (InputStream in = getClass().getClassLoader().getResourceAsStream("s10k/tool/version.properties")) {
			props.load(in);
			version = props.getProperty("version");
		} catch (IOException e) {
			// ignore and continue
		}
		return new String[] { version != null ? version : "Unknown" };
	}

}
