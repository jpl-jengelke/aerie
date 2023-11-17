package gov.nasa.jpl.aerie.timeline.ops.coalesce

import gov.nasa.jpl.aerie.timeline.Segment
import gov.nasa.jpl.aerie.timeline.ops.TimelineOps

interface Coalesce<V: Any, P: Any>: TimelineOps<Segment<V>, P> {
  fun Segment<V>.shouldCoalesce(other: Segment<V>): Boolean

  /**
   * @suppress
   */
  fun coalesce(): P {
    TODO()
  }
}
