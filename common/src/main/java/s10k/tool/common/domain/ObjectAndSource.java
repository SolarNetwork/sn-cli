package s10k.tool.common.domain;

import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;

import com.fasterxml.jackson.annotation.JsonIgnore;

import net.solarnetwork.domain.datum.ObjectDatumKind;
import net.solarnetwork.util.StringUtils;

/**
 * An object and source, uniquely identifying a datum stream.
 * 
 * @param kind     the object kind
 * @param objectId the object ID
 * @param sourceId the source ID
 */
@RegisterReflectionForBinding
public record ObjectAndSource(ObjectDatumKind kind, Long objectId, String sourceId)
		implements Comparable<ObjectAndSource> {

	@Override
	public int compareTo(ObjectAndSource o) {
		int result = kind.compareTo(o.kind);
		if (result == 0) {
			result = objectId.compareTo(o.objectId);
			if (result == 0) {
				result = StringUtils.naturalSortCompare(sourceId, o.sourceId, true);
			}
		}
		return result;
	}

	/**
	 * Test if the node and source are both populated.
	 * 
	 * @return {@code true} if both {@code nodeId} and {@code sourceId} are not
	 *         empty
	 */
	@JsonIgnore
	public boolean isValid() {
		return kind != null && objectId != null && objectId.longValue() != 0 && sourceId != null && !sourceId.isBlank();
	}

}
