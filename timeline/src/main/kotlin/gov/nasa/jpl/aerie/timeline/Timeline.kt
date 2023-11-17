package gov.nasa.jpl.aerie.timeline

import gov.nasa.jpl.aerie.timeline.ops.TimelineOps

data class Timeline<V: IntervalLike<V>, T: Any>(
    override val ctor: (TimelineOps<V, T>) -> T,
    private val collector: (Interval) -> List<V>
): TimelineOps<V, T> {
  override fun collect(bounds: Interval) = collector(bounds)
}
