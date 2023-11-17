package gov.nasa.jpl.aerie.timeline.ops.coalesce

import gov.nasa.jpl.aerie.timeline.Segment

interface CoalesceNaive<V: Any, P: Any>: Coalesce<V, P> {
  override fun Segment<V>.shouldCoalesce(other: Segment<V>): Boolean = value == other.value
}
