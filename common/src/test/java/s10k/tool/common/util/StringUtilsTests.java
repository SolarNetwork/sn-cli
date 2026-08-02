package s10k.tool.common.util;

import static org.assertj.core.api.BDDAssertions.then;

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Test cases for the {@link StringUtils} class.
 */
public class StringUtilsTests {

	private enum TestOrders {
		One, Two, Three,
	}

	@Test
	public void orderByList_null() {
		// WHEN
		final List<String> result = StringUtils.orderByList(null, TestOrders.class);

		// THEN
		then(result).as("Null input returns null").isNull();
	}

	@Test
	public void orderByList_empty() {
		// WHEN
		final List<String> result = StringUtils.orderByList(new String[0], TestOrders.class);

		// THEN
		then(result).as("Empty input returns null").isNull();
	}

	@Test
	public void orderByList_noValid() {
		// WHEN
		final List<String> result = StringUtils.orderByList(new String[] { "not one", "another not one" },
				TestOrders.class);

		// THEN
		then(result).as("Entirely invalid input returns null").isNull();
	}

	@Test
	public void orderByList_someValid() {
		// WHEN
		final List<String> result = StringUtils.orderByList(new String[] { "One", "not valid", "Three" },
				TestOrders.class);

		// THEN
		then(result).as("Valid keys returned").containsExactly("one", "three");
	}

	@Test
	public void orderByList_caseInsensitiveMatch() {
		// WHEN
		final List<String> result = StringUtils.orderByList(new String[] { "ONE", "tWo~", "three" }, TestOrders.class);

		// THEN
		then(result).as("Keys match in case-insensitive manner").containsExactly("one", "two~", "three");
	}

}
