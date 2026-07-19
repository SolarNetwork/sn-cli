package s10k.tool.c2c.domain;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SequencedCollection;
import java.util.Spliterator;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import net.solarnetwork.domain.datum.Datum;
import net.solarnetwork.domain.datum.DatumAuxiliaryRecord;

/**
 * Basic implementation of {@link CloudDatumStreamQueryResult}.
 */
@RegisterReflectionForBinding
@JsonIgnoreProperties({ "empty" })
@JsonPropertyOrder({ "returnedResultCount", "usedQueryFilter", "nextQueryFilter", "results", "auxiliary" })
public class BasicCloudDatumStreamQueryResult implements CloudDatumStreamQueryResult {

	private final @Nullable Map<String, Object> usedQueryFilter;
	private final @Nullable Map<String, Object> nextQueryFilter;
	private final SequencedCollection<Datum> results;
	private final @Nullable SequencedCollection<DatumAuxiliaryRecord> auxiliary;

	/**
	 * Constructor.
	 *
	 * @param results the results, or {@code null}
	 */
	public BasicCloudDatumStreamQueryResult(@Nullable SequencedCollection<Datum> results) {
		this(null, null, results);
	}

	/**
	 * Constructor.
	 *
	 * @param usedQueryFilter the used query filter, or {@code null}
	 * @param nextQueryFilter the next query filter, or {@code null}
	 * @param results         the results, or {@code null}
	 */
	public BasicCloudDatumStreamQueryResult(@Nullable Map<String, Object> usedQueryFilter,
			@Nullable Map<String, Object> nextQueryFilter, @Nullable SequencedCollection<Datum> results) {
		this(usedQueryFilter, nextQueryFilter, results, null);
	}

	/**
	 * Constructor.
	 *
	 * @param usedQueryFilter the used query filter, or {@code null}
	 * @param nextQueryFilter the next query filter, or {@code null}
	 * @param results         the results, or {@code null}
	 * @param auxiliary       the auxiliary, or {@code null}
	 */
	@JsonCreator
	public BasicCloudDatumStreamQueryResult(
			@JsonProperty(value = "usedQueryFilter", required = false) @Nullable Map<String, Object> usedQueryFilter,
			@JsonProperty(value = "nextQueryFilter", required = false) @Nullable Map<String, Object> nextQueryFilter,
			@JsonProperty(value = "results", required = false) @Nullable SequencedCollection<Datum> results,
			@JsonProperty(value = "auxiliary", required = false) @Nullable SequencedCollection<DatumAuxiliaryRecord> auxiliary) {
		super();
		this.usedQueryFilter = usedQueryFilter;
		this.nextQueryFilter = nextQueryFilter;
		this.results = (results != null ? results : List.of());
		this.auxiliary = auxiliary;
	}

	@Override
	public Iterator<Datum> iterator() {
		return results.iterator();
	}

	@Override
	public void forEach(Consumer<? super Datum> action) {
		results.forEach(action);
	}

	@Override
	public Spliterator<Datum> spliterator() {
		return results.spliterator();
	}

	@Override
	public final SequencedCollection<Datum> getResults() {
		return results;
	}

	@Override
	public @Nullable Map<String, Object> getUsedQueryFilter() {
		return usedQueryFilter;
	}

	@Override
	public final @Nullable Map<String, Object> getNextQueryFilter() {
		return nextQueryFilter;
	}

	@Override
	public @Nullable SequencedCollection<DatumAuxiliaryRecord> getAuxiliary() {
		return auxiliary;
	}

}
