package s10k.tool.common.domain;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * A date range interval.
 */
@RegisterReflectionForBinding
@JsonPropertyOrder({ "startDate", "endDate", "timeZone", "yearCount", "monthCount", "dayCount" })
public record DateRangeInfo(@JsonProperty("startDate") LocalDateTime startDate,
		@JsonProperty("endDate") LocalDateTime endDate, @JsonProperty("timeZone") ZoneId timeZone) {

	/**
	 * Get a count of days this interval spans (inclusive).
	 * 
	 * <p>
	 * This is the complete number of calendar days the data is present in, so
	 * partial days are counted as full days. For example, the interval
	 * {@code 2008-08-11/2009-08-05} returns 360.
	 * </p>
	 * 
	 * @return count of days within the interval
	 */
	@JsonGetter("dayCount")
	public final long dayCount() {
		return ChronoUnit.DAYS.between(startDate.atZone(timeZone), endDate.atZone(timeZone)) + 1;
	}

	/**
	 * Get a count of months this interval spans (inclusive).
	 * 
	 * <p>
	 * This is the complete number of calendar months the data is present in, so
	 * partial months are counted as full months. For example, the interval
	 * {@code 2008-08-11/2009-08-05} returns 13.
	 * </p>
	 * 
	 * @return count of months within the interval
	 */
	@JsonGetter("monthCount")
	public final long monthCount() {
		return ChronoUnit.MONTHS.between(startDate.atZone(timeZone).with(TemporalAdjusters.firstDayOfMonth()),
				endDate.atZone(timeZone).with(TemporalAdjusters.firstDayOfMonth())) + 1;
	}

	/**
	 * Get a count of years this interval spans (inclusive).
	 * 
	 * <p>
	 * This is the complete number of calendar years the data is present in, so
	 * partial years are counted as full years. For example, the interval
	 * {@code 2008-08-11/2009-08-05} returns 2.
	 * </p>
	 * 
	 * @return count of months within the interval
	 */
	@JsonGetter("yearCount")
	public final long yearCount() {
		return ChronoUnit.YEARS.between(startDate.with(TemporalAdjusters.firstDayOfYear()),
				endDate.with(TemporalAdjusters.firstDayOfYear())) + 1;
	}

}
