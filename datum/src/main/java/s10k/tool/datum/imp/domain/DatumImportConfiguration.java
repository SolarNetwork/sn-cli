package s10k.tool.datum.imp.domain;

import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

import com.fasterxml.jackson.annotation.JsonProperty;

/*-
    {
      "name": "Test Import",
      "stage": true,
      "inputConfiguration": {
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
    }
 */

/**
 * Datum import configuration.
 */
@RegisterReflectionForBinding
public record DatumImportConfiguration(
// @formatter:off
		@JsonProperty("name") String name,
		@JsonProperty("stage") boolean stage,
		@JsonProperty(value = "batchSize", required = false) @Nullable Integer batchSize,
		@JsonProperty(value = "groupKey", required = false) @Nullable String groupKey,
		@JsonProperty("inputConfiguration") DatumInputServiceConfiguration inputConfiguration
		// @formatter:on
) {

}
