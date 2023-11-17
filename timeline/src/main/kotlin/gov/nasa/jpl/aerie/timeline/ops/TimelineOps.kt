package gov.nasa.jpl.aerie.timeline.ops

import gov.nasa.jpl.aerie.timeline.Interval
import gov.nasa.jpl.aerie.timeline.IntervalLike

interface TimelineOps<V: IntervalLike, T: Any> {
  fun collect(bounds: Interval): List<V>
  fun specialize() = ctor(this)

  val ctor: (TimelineOps<V, T>) -> T
}
