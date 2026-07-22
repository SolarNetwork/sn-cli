package s10k.tool.common.domain;

/**
 * A merge mode enumeration used in various PATCH APIs.
 */
public enum MergeMode {

	/** A simple merge of top-level properties only. */
	Simple,

	/** A recursive merge of objects. */
	RecursiveObjects,

	/** A recursive merge of objects and arrays. */
	RecursiveObjectsAndArrays,

}
