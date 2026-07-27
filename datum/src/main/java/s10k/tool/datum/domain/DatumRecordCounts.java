package s10k.tool.datum.domain;

import java.time.Instant;

import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Datum record counts.
 */
@RegisterReflectionForBinding
@JsonPropertyOrder({ "date", "datumTotalCount", "datumCount", "datumHourlyCount", "datumDailyCount",
		"datumMonthlyCount" })
public record DatumRecordCounts(
// @formatter:off
		  @JsonProperty(value = "date", required = false) @Nullable Instant date
		, @JsonProperty(value = "datumCount", required = false) @Nullable Long datumCount
		, @JsonProperty(value = "datumHourlyCount", required = false) @Nullable Long datumHourlyCount
		, @JsonProperty(value = "datumDailyCount", required = false) @Nullable Integer datumDailyCount
		, @JsonProperty(value = "datumMonthlyCount", required = false) @Nullable Integer datumMonthlyCount
		// @formatter:on
) {

	/**
	 * Get the sum total of all datum counts.
	 *
	 * @return the sum total of the datum count properties
	 */
	@JsonProperty("datumTotalCount")
	public final long datumTotalCount() {
		long t = 0;
		if (datumCount != null) {
			t += datumCount;
		}
		if (datumHourlyCount != null) {
			t += datumHourlyCount;
		}
		if (datumDailyCount != null) {
			t += datumDailyCount;
		}
		if (datumMonthlyCount != null) {
			t += datumMonthlyCount;
		}
		return t;
	}

}
