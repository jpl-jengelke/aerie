package gov.nasa.jpl.aerie.timeline.ops

import gov.nasa.jpl.aerie.timeline.*
import gov.nasa.jpl.aerie.timeline.ops.coalesce.Coalesce
import gov.nasa.jpl.aerie.timeline.util.map2

interface SerialOps<V : Any, P: Any>: SegmentOps<V, P>, Coalesce<V, P> {
  fun assignGaps(other: SerialOps<V, P>) = other.set(this)
  fun set(other: SerialOps<V, P>) = map2Values(other, BinaryOperation.combineOrIdentity { _, r, _ -> r })

  fun <W: Any, Q: Any> map2Values(other: SerialOps<W, Q>, op: BinaryOperation<V, W, V?>) = map2ValuesInto(ctor, other, op)
  fun <W: Any, POther: Any, Out: Any, PInto: Any> map2ValuesInto(ctor: (TimelineOps<Segment<Out>, PInto>) -> PInto, other: SerialOps<W, POther>, op: BinaryOperation<V, W, Out?>) =
      Timeline(ctor) { bounds -> map2(collect(bounds), other.collect(bounds), op) }.specialize()
}
