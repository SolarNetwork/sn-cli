package s10k.tool.common.util;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.toml.TomlMapper;

import net.solarnetwork.codec.JsonUtils;
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

	/** The config file name. */
	public static final String CONFIG_FILENAME = "config";

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
	 * Load configuration for a profile.
	 * 
	 * @param name the name of the profile to load the configuration for
	 * @return the profile configuration, or {@code null} if none available
	 */
	public static @Nullable Map<String, ?> profileConfiguration(@Nullable String name) {
		final Path configPath = userConfigurationDir().resolve(CONFIG_FILENAME);
		if (Files.isReadable(configPath)) {
			try {
				TomlMapper mapper = new TomlMapper();
				try (InputStream in = Files.newInputStream(configPath)) {
					JsonNode root = mapper.readTree(in);
					JsonNode profileNode;
					if (name == null || name.isBlank()) {
						profileNode = root;
					} else {
						profileNode = root.path("profile").path(name);
					}
					if (profileNode.isObject()) {
						if (name == null || name.isBlank()) {
							// extract all but "profile"
							Map<String, Object> result = new LinkedHashMap<>(profileNode.size());
							profileNode.forEachEntry((k, v) -> {
								if (!"profile".equals(k)) {
									result.put(k, v);
								}
							});
							return result;
						}
						return mapper.treeToValue(profileNode, JsonUtils.STRING_MAP_TYPE);
					}
				}
			} catch (Exception e) {
				log.warn("Error reading configuration file [{}]: {}", configPath, e.toString());
			}
		}
		return null;
	}

	/**
	 * Load a profile.
	 * 
	 * @param name the name of the profile to load, or {@code null} for the
	 *             "default" profile
	 * @return the profile, or {@code null} if the profile does not exist
	 */
	public static @Nullable ProfileInfo profile(String name) {
		// load from user configuration
		Path credPath = userConfigurationDir().resolve(CREDENTIALS_FILENAME);
		if (Files.isReadable(credPath)) {
			try {
				TomlMapper mapper = new TomlMapper();
				try (InputStream in = Files.newInputStream(credPath)) {
					JsonNode root = mapper.readTree(in);
					JsonNode credsNode;
					if (name == null || name.isBlank()) {
						credsNode = root;
					} else {
						credsNode = root.findValue(name);
					}
					if (credsNode != null) {
						SnTokenCredentials creds = mapper.treeToValue(credsNode, SnTokenCredentials.class);
						if (creds.hasCredentials()) {
							Map<String, ?> config = profileConfiguration(name);
							return new ProfileInfo(name, creds, config);
						}
					}
				}
			} catch (Exception e) {
				log.warn("Error reading credentials file [{}]: {}", credPath, e.toString());
			}
		}
		return null;
	}

}
