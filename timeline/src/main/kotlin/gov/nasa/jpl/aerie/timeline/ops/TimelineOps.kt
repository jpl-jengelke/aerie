package gov.nasa.jpl.aerie.timeline.ops

import gov.nasa.jpl.aerie.timeline.Interval
import gov.nasa.jpl.aerie.timeline.IntervalLike
import gov.nasa.jpl.aerie.timeline.Segment
import gov.nasa.jpl.aerie.timeline.Timeline

interface TimelineOps<V: IntervalLike, T: Any> {
  fun collect(bounds: Interval): List<V>

  /** @suppress */
  val ctor: (TimelineOps<V, T>) -> T
  /** @suppress */
  fun specialize() = ctor(this)

  /**
   * **UNSAFE**
   */
  fun map(f: (V) -> V) = mapInto(ctor, f)
  fun <W: IntervalLike, PInto: Any> mapInto(ctor: (TimelineOps<W, PInto>) -> PInto, f: (V) -> W) =
      Timeline(ctor) { bounds -> collect(bounds).map { f(it) }}.specialize()

  fun filter(f: (V) -> Boolean) = Timeline(ctor) { bounds -> collect(bounds).filter(f) }.specialize()

  fun all(f: (V) -> Boolean, bounds: Interval) = collect(bounds).all(f)
  fun any(f: (V) -> Boolean, bounds: Interval) = collect(bounds).any(f)
}
