package gov.nasa.jpl.aerie.timeline.ops

import gov.nasa.jpl.aerie.merlin.protocol.types.RealDynamics
import gov.nasa.jpl.aerie.timeline.*
import gov.nasa.jpl.aerie.timeline.util.map2Lists

interface ProfileOps<V : Any, P: Any>: TimelineOps<Segment<V>, P> {
  fun assignGaps(other: ProfileOps<V, P>) = other.set(this)
  fun set(other: ProfileOps<V, P>) = map2Values(other, BinaryOperation.combineOrIdentity { _, r, _ -> r })

  fun mapValues(f: (Segment<V>) -> V) = mapValuesInternal(ctor, f)
  fun mapValuesWindows(f: (Segment<V>) -> Boolean) = mapValuesInternal(::Windows, f)
  fun mapValuesReal(f: (Segment<V>) -> RealDynamics) = mapValuesInternal(::Real, f)
  fun <W : Any> mapValuesDiscrete(f: (Segment<V>) -> W) = mapValuesInternal(::Discrete, f)
  private fun <W: Any, Q: Any> mapValuesInternal(ctor: (TimelineOps<Segment<W>, Q>) -> Q, f: (Segment<V>) -> W) =
      Timeline(ctor) { bounds -> collect(bounds).map { it.mapValue(f) }}.specialize()

  fun <W: Any, Q: Any> map2Values(other: ProfileOps<W, Q>, op: BinaryOperation<V, W, V?>) = map2ValuesInternal(ctor, other, op)
  fun <W: Any, Q: Any> map2ValuesWindows(other: ProfileOps<W, Q>, op: BinaryOperation<V, W, Boolean?>) = map2ValuesInternal(::Windows, other, op)
  fun <W: Any, Q: Any> map2ValuesReal(other: ProfileOps<W, Q>, op: BinaryOperation<V, W, RealDynamics?>) = map2ValuesInternal(::Real, other, op)
  fun <W: Any, Q: Any, Out: Any> map2ValuesDiscrete(other: ProfileOps<W, Q>, op: BinaryOperation<V, W, Out?>) = map2ValuesInternal(::Discrete, other, op)
  private fun <W: Any, Q: Any, Out: Any, R: Any> map2ValuesInternal(ctor: (TimelineOps<Segment<Out>, R>) -> R, other: ProfileOps<W, Q>, op: BinaryOperation<V, W, Out?>) =
      Timeline(ctor) { bounds -> map2Lists(collect(bounds), other.collect(bounds), op) }.specialize()

  fun filter(f: (Segment<V>) -> Boolean) = Timeline(ctor) { bounds -> collect(bounds).filter(f) }.specialize()
}
