package s10k.tool.nodes.util;

import static com.github.freva.asciitable.HorizontalAlign.LEFT;
import static com.github.freva.asciitable.HorizontalAlign.RIGHT;

import com.github.freva.asciitable.Column;

import s10k.tool.nodes.domain.NodeInfo;

/**
 * Utilities for nodes.
 */
public final class NodeUtils {

	/**
	 * Get node info tabular structure columns.
	 * 
	 * @return the columns
	 */
	public static Column[] nodeInfoColumns() {
		// @formatter:off
		return new Column[] {
				new Column().header("Node ID").dataAlign(RIGHT),
				new Column().header("Created").dataAlign(LEFT),
				new Column().header("Public").dataAlign(LEFT),
				new Column().header("Country").dataAlign(LEFT),
				new Column().header("Time Zone").dataAlign(LEFT),
			};
		// @formatter:on
	}

	/**
	 * Convert node info into a tabular structure.
	 * 
	 * @param info the node info to convert
	 * @return the tabular data
	 */
	public static Object[] nodeInfoRow(NodeInfo info) {
		// @formatter:off
		return new Object[] {
				info.nodeId(),
				info.created(),
				!info.requiresAuthorization(),
				info.location() != null ? info.location().getCountry() : null,
				info.location() != null ? info.location().getTimeZoneId() : null,
			};
		// @formatter:on
	}

}
