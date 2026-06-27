package s10k.tool.c2c.util;

import static org.assertj.core.api.BDDAssertions.then;

import java.time.Period;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Test cases for the {@link CloudIntegrationsUtils} class.
 */
public class CloudIntegrationUtilsTests {

	@ParameterizedTest
	@ValueSource(strings = {
	// @formatter:off
			  "s10k.c2c.i9n.also"
			, "s10k.c2c.i9n.egauge"
			, "s10k.c2c.i9n.enphase"
			, "s10k.c2c.i9n.fronius"
			, "s10k.c2c.i9n.locus"
			, "s10k.c2c.i9n.owm"
			, "s10k.c2c.i9n.sma"
			, "s10k.c2c.i9n.solaredge.v1"
			, "s10k.c2c.i9n.solcast"
			, "s10k.c2c.i9n.solrenview"
			// @formatter:on
	})
	public void localizedCloudIntegrationServiceNames(String serviceId) {
		// WHEN
		final String result = CloudIntegrationsUtils.integrationServiceLocalizedName(serviceId);

		// THEN
		// @formatter:off
		then(result)
			.isNotNull()
			.as("Is not the input value")
			.isNotEqualTo(serviceId)
			.satisfies(s -> {
				final int idx = serviceId.lastIndexOf('.');
				then(s)
					.as("Is not just the final component of the service ID")
					.isNotEqualTo(serviceId.substring(idx + 1))
					;
			})
			;
		// @formatter:on
	}

	@Test
	public void localizedCloudIntegrationServiceNames_unknown() {
		// GIVEN
		final String serviceId = "foo.bar.bam";

		// WHEN
		final String result = CloudIntegrationsUtils.integrationServiceLocalizedName(serviceId);

		// THEN
		// @formatter:off
		then(result)
			.as("Unknown service uses last dot-delimited component value")
			.isEqualTo("bam")
			;
		// @formatter:on
	}

	@Test
	public void localizedCloudIntegrationServiceNames_unknown_noDots() {
		// GIVEN
		final String serviceId = "Has No Dots";

		// WHEN
		final String result = CloudIntegrationsUtils.integrationServiceLocalizedName(serviceId);

		// THEN
		// @formatter:off
		then(result)
			.as("Unknown service without any dots returns original value")
			.isEqualTo(serviceId)
			;
		// @formatter:on
	}

	@ParameterizedTest
	@ValueSource(strings = {
	// @formatter:off
			  "s10k.c2c.ds.also"
			, "s10k.c2c.ds.egauge"
			, "s10k.c2c.ds.enphase"
			, "s10k.c2c.ds.fronius"
			, "s10k.c2c.ds.locus"
			, "s10k.c2c.ds.owm.day"
			, "s10k.c2c.ds.owm.forecast"
			, "s10k.c2c.ds.owm.weather"
			, "s10k.c2c.ds.sma"
			, "s10k.c2c.ds.solaredge.v1"
			, "s10k.c2c.ds.solcast.irr"
			, "s10k.c2c.ds.solrenview"
			// @formatter:on
	})
	public void localizedCloudDatumStreamServiceNames(String serviceId) {
		// WHEN
		final String result = CloudIntegrationsUtils.datumStreamServiceLocalizedName(serviceId);

		// THEN
		// @formatter:off
		then(result)
			.isNotNull()
			.as("Is not the input value")
			.isNotEqualTo(serviceId)
			.satisfies(s -> {
				final int idx = serviceId.lastIndexOf('.');
				then(s)
					.as("Is not just the final component of the service ID")
					.isNotEqualTo(serviceId.substring(idx + 1))
					;
			})
			;
		// @formatter:on
	}

	@Test
	public void localizedCloudDatumStreamServiceNames_unknown() {
		// GIVEN
		final String serviceId = "foo.bar.bam";

		// WHEN
		final String result = CloudIntegrationsUtils.datumStreamServiceLocalizedName(serviceId);

		// THEN
		// @formatter:off
		then(result)
			.as("Unknown service uses last dot-delimited component value")
			.isEqualTo("bam")
			;
		// @formatter:on
	}

	@Test
	public void localizedCloudDatumStreamServiceNames_unknown_noDots() {
		// GIVEN
		final String serviceId = "Has No Dots";

		// WHEN
		final String result = CloudIntegrationsUtils.datumStreamServiceLocalizedName(serviceId);

		// THEN
		// @formatter:off
		then(result)
			.as("Unknown service without any dots returns original value")
			.isEqualTo(serviceId)
			;
		// @formatter:on
	}

	@Test
	public void comparePeriods_equal() {
		// GIVEN
		final Period l = Period.of(1, 2, 3);
		final Period r = Period.of(1, 2, 3);

		// THEN
		then(CloudIntegrationsUtils.comparePeriods(l, r)).isEqualTo(0);
		then(CloudIntegrationsUtils.comparePeriods(r, l)).isEqualTo(0);
	}

	@Test
	public void comparePeriods_equal_normalized() {
		// GIVEN
		final Period l = Period.of(0, 12, 0);
		final Period r = Period.of(1, 0, 0);

		// THEN
		then(CloudIntegrationsUtils.comparePeriods(l, r)).isEqualTo(0);
		then(CloudIntegrationsUtils.comparePeriods(r, l)).isEqualTo(0);
	}

	@Test
	public void comparePeriods_less() {
		// GIVEN
		final Period l = Period.of(0, 1, 2);
		final Period r = Period.of(1, 2, 3);

		// THEN
		then(CloudIntegrationsUtils.comparePeriods(l, r)).isLessThan(0);
		then(CloudIntegrationsUtils.comparePeriods(r, l)).isGreaterThan(0);
	}

}
