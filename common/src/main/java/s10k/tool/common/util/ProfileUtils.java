package s10k.tool.common.util;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;

import s10k.tool.common.domain.ProfileInfo;
import s10k.tool.common.domain.SnTokenCredentials;

/**
 * Helpers for dealing with profiles.
 */
public final class ProfileUtils {

	/** The user configuration directory name. */
	public static final String USER_CONFIG_DIR_NAME = ".s10k";

	/** The credentials file name. */
	public static final String CREDENTIALS_FILENAME = "credentials";

	private static final Logger log = LoggerFactory.getLogger(ProfileUtils.class);

	private ProfileUtils() {
		// not available
	}

	/**
	 * Get the path to the user configuration directory.
	 * 
	 * @return the path
	 */
	public static Path userConfigurationDir() {
		return Paths.get(System.getProperty("user.home"), USER_CONFIG_DIR_NAME);
	}

	/**
	 * Load a profile.
	 * 
	 * @param name the name of the profile to load
	 * @return the profile
	 */
	public static ProfileInfo profile(String name) {

		SnTokenCredentials creds = null;

		// load from user configuration
		Path credPath = userConfigurationDir().resolve(CREDENTIALS_FILENAME);
		if (Files.isReadable(credPath)) {
			try {
				TomlMapper mapper = new TomlMapper();
				try (InputStream in = Files.newInputStream(credPath)) {
					JsonNode root = mapper.readTree(in);
					JsonNode credsNode = root.findValue(name);
					if (credsNode != null) {
						creds = mapper.treeToValue(credsNode, SnTokenCredentials.class);
					}
				}
			} catch (Exception e) {
				log.warn("Error reading credentials file [{}]: {}", credPath, e.toString());
			}
		}

		return new ProfileInfo(name, creds);
	}

}
