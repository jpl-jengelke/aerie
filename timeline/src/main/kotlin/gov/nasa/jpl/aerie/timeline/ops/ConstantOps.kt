package gov.nasa.jpl.aerie.timeline.ops

/**
 * Operations for segment-valued timelines whose payloads
 * represent constant values.
 *
 * (
 */
interface ConstantOps<V: Any, P: Any>: SegmentOps<V, P> {
  // equalTo, notEqualTo
}
