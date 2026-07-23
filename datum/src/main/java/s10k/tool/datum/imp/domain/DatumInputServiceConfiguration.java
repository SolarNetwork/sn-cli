package s10k.tool.datum.imp.domain;

import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

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

}
