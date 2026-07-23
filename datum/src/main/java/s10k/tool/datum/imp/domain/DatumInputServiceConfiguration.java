package s10k.tool.datum.imp.domain;

import java.util.LinkedHashMap;
import java.util.Map;

import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

import net.solarnetwork.codec.JsonUtils;
import s10k.tool.common.domain.ServiceConfiguration;

/*-
      {
        "name": "Test Input",
        "serviceIdentifier": "net.solarnetwork.central.datum.imp.standard.BasicCsvDatumImportInputFormatService",
        "timeZoneId": "America/New_York",
        "serviceProperties": {
          "dateFormat": "MM/dd/yyyy HH:mm:ss",
          "nodeIdColumn": "2",
          "headerRowCount": "0",
          "sourceIdColumn": "3",
          "dateColumnsValue": "1"
        }
      }
 */

/**
 * A datum import service configuration.
 */
@RegisterReflectionForBinding
public class DatumInputServiceConfiguration extends ServiceConfiguration {

	private @Nullable String timeZoneId;

	/**
	 * Default constructor.
	 */
	public DatumInputServiceConfiguration() {
		super();
	}

	/**
	 * Get the time zone ID.
	 * 
	 * @return the time zone ID
	 */
	public final @Nullable String getTimeZoneId() {
		return timeZoneId;
	}

	/**
	 * Set the time zone ID.
	 * 
	 * @param timeZoneId the time zone ID to set
	 */
	public final void setTimeZoneId(@Nullable String timeZoneId) {
		this.timeZoneId = timeZoneId;
	}

	/**
	 * Get a mapping of this entity's settings.
	 * 
	 * @return the settings
	 */
	public Map<String, Object> toSettings() {
		Map<String, Object> result = new LinkedHashMap<>(9);
		result.put("name", getName());
		result.put("serviceIdentifier", getServiceIdentifier());
		result.put("timeZoneId", timeZoneId);
		if (getServiceProperties() != null) {
			// perform a deep copy here
			result.put(SERVICE_PROPERTIES_KEY, JsonUtils.getStringMapFromObject(getServiceProperties()));
		}
		return result;
	}

}
