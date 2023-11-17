package gov.nasa.jpl.aerie.timeline.ops

import gov.nasa.jpl.aerie.merlin.protocol.types.RealDynamics
import gov.nasa.jpl.aerie.timeline.*
import gov.nasa.jpl.aerie.timeline.types.Discrete
import gov.nasa.jpl.aerie.timeline.types.Real
import gov.nasa.jpl.aerie.timeline.types.Windows
import gov.nasa.jpl.aerie.timeline.util.map2Lists

interface ProfileOps<V : Any, P: Any>: TimelineOps<Segment<V>, P> {
  fun assignGaps(other: ProfileOps<V, P>) = other.set(this)
  fun set(other: ProfileOps<V, P>) = map2Values(other, BinaryOperation.combineOrIdentity { _, r, _ -> r })

  fun mapValues(f: (Segment<V>) -> V) = mapValuesInto(ctor, f)
  fun <W: Any, PInto: Any> mapValuesInto(ctor: (TimelineOps<Segment<W>, PInto>) -> PInto, f: (Segment<V>) -> W) =
      Timeline(ctor) { bounds -> collect(bounds).map { it.mapValue(f) }}.specialize()

  fun <W: Any, Q: Any> map2Values(other: ProfileOps<W, Q>, op: BinaryOperation<V, W, V?>) = map2ValuesInto(ctor, other, op)
  fun <W: Any, POther: Any, Out: Any, PInto: Any> map2ValuesInto(ctor: (TimelineOps<Segment<Out>, PInto>) -> PInto, other: ProfileOps<W, POther>, op: BinaryOperation<V, W, Out?>) =
      Timeline(ctor) { bounds -> map2Lists(collect(bounds), other.collect(bounds), op) }.specialize()

  fun filter(f: (Segment<V>) -> Boolean) = Timeline(ctor) { bounds -> collect(bounds).filter(f) }.specialize()
}
