package s10k.tool.c2c.domain;

import java.util.Map;
import java.util.SequencedCollection;

import org.jspecify.annotations.Nullable;

import net.solarnetwork.domain.datum.Datum;
import net.solarnetwork.domain.datum.DatumAuxiliaryRecord;

public interface CloudDatumStreamQueryResult extends Iterable<Datum> {

	/**
	 * Test if there are no results available.
	 *
	 * @return {@code true} if there are no results
	 */
	default boolean isEmpty() {
		return (getReturnedResultCount() == 0);
	}

	/**
	 * Get the returned result count.
	 *
	 * <p>
	 * This is an alias of {@link #getReturnedResultCount()}.
	 * </p>
	 *
	 * @return the number of returned results
	 */
	default int size() {
		return getReturnedResultCount();
	}

	/**
	 * Get the number of results included in {@link #getResults()}.
	 *
	 * @return the number of returned results
	 */
	default int getReturnedResultCount() {
		final var results = getResults();
		return (results != null ? results.size() : 0);
	}

	/**
	 * Get a query filter used to return these results.
	 *
	 * <p>
	 * This may return different values that provided to the query, such as a
	 * different date range due to query constraints.
	 * </p>
	 *
	 * @return a query filter, or {@code null}
	 */
	default @Nullable Map<String, Object> getUsedQueryFilter() {
		return null;
	}

	/**
	 * Get a query filter configured to return the next set of results, if any.
	 *
	 * @return a query filter, or {@code null}
	 */
	default @Nullable Map<String, Object> getNextQueryFilter() {
		return null;
	}

	/**
	 * Get the results.
	 *
	 * <p>
	 * These are the same results returned by {@link Iterable#iterator()}.
	 * </p>
	 *
	 * @return the results, never {@code node}
	 */
	SequencedCollection<Datum> getResults();

	/**
	 * Get any generated auxiliary records.
	 *
	 * <p>
	 * These records are used to capture observations or other metadata about the
	 * resolved datum.
	 * </p>
	 *
	 * @return the auxiliary records, or {@code null}
	 */
	default @Nullable SequencedCollection<DatumAuxiliaryRecord> getAuxiliary() {
		return null;
	}

}
