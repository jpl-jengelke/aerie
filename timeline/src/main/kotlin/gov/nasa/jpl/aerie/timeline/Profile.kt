package gov.nasa.jpl.aerie.timeline

import gov.nasa.jpl.aerie.merlin.protocol.types.RealDynamics

interface ProfileOps<V : Any, P: Any>: TimelineOps<Segment<V>, P> {
  fun assignGaps(other: ProfileOps<V, P>) = other.set(this)
  fun set(other: ProfileOps<V, P>) = map2Values(other, BinaryOperation.combineOrIdentity { _, r, _ -> r })

  fun mapValues(f: (Segment<V>) -> V) = mapValuesInternal(ctor, f)
  fun mapValuesWindows(f: (Segment<V>) -> Boolean) = mapValuesInternal(::Windows, f)
  fun mapValuesReal(f: (Segment<V>) -> RealDynamics) = mapValuesInternal(::Real, f)
  fun <W : Any> mapValuesDiscrete(f: (Segment<V>) -> W) = mapValuesInternal<W, Discrete<W>>(::Discrete, f)
  private fun <W: Any, Q: Any> mapValuesInternal(ctor: (TimelineOps<Segment<W>, Q>) -> Q, f: (Segment<V>) -> W) =
      Timeline(ctor) { bounds -> collect(bounds).map { it.mapValue(f) }}.specialize()

  fun <W: Any, Q: Any> map2Values(other: ProfileOps<W, Q>, op: BinaryOperation<V, W, V?>) = map2ValuesInternal(ctor, other, op)
  fun <W: Any, Q: Any> map2ValuesWindows(other: ProfileOps<W, Q>, op: BinaryOperation<V, W, Boolean?>) = map2ValuesInternal(::Windows, other, op)
  fun <W: Any, Q: Any> map2ValuesReal(other: ProfileOps<W, Q>, op: BinaryOperation<V, W, RealDynamics?>) = map2ValuesInternal(::Real, other, op)
  fun <W: Any, Q: Any, Out: Any> map2ValuesDiscrete(other: ProfileOps<W, Q>, op: BinaryOperation<V, W, Out?>) = map2ValuesInternal<W, Q, Out, Discrete<Out>>(::Discrete, other, op)
  private fun <W: Any, Q: Any, Out: Any, R: Any> map2ValuesInternal(ctor: (TimelineOps<Segment<Out>, R>) -> R, other: ProfileOps<W, Q>, op: BinaryOperation<V, W, Out?>) =
      Timeline(ctor) { bounds -> map2Lists(collect(bounds), other.collect(bounds), op) }.specialize()

  fun filter(f: (Segment<V>) -> Boolean) = Timeline(ctor) { bounds -> collect(bounds).filter(f) }.specialize()
}

data class Profile<V : Any>(private val timeline: Timeline<Segment<V>, Profile<V>>): ProfileOps<V, Profile<V>>, TimelineOps<Segment<V>, Profile<V>> by timeline

fun <Left, Right, Out> map2Lists(
    left: List<Segment<Left & Any>>,
    right: List<Segment<Right & Any>>,
    op: BinaryOperation<Left, Right, Out?>
): List<Segment<Out & Any>> {
  val result = mutableListOf<Segment<Out & Any>>()

  var leftIndex = 0
  var rightIndex = 0

  var leftSegment: Segment<Left & Any>?
  var rightSegment: Segment<Right & Any>?
  var remainingLeftSegment: Segment<Left & Any>? = null
  var remainingRightSegment: Segment<Right & Any>? = null

  while (
      leftIndex < left.size ||
      rightIndex < right.size ||
      remainingLeftSegment != null ||
      remainingRightSegment != null
  ) {
    if (remainingLeftSegment != null) {
      leftSegment = remainingLeftSegment
      remainingLeftSegment = null
    } else if (leftIndex < left.size) {
      leftSegment = left[leftIndex++]
    } else {
      leftSegment = null
    }
    if (remainingRightSegment != null) {
      rightSegment = remainingRightSegment
      remainingRightSegment = null
    } else if (rightIndex < right.size) {
      rightSegment = right[rightIndex++]
    } else {
      rightSegment = null
    }

    if (leftSegment == null) {
      val resultingSegment = rightSegment!!.mapValue { op(null, it.value, it.interval) }.transpose()
      if (resultingSegment != null) result.add(resultingSegment)
    } else if (rightSegment == null) {
      val resultingSegment = leftSegment.mapValue { op(it.value, null, it.interval) }.transpose()
      if (resultingSegment != null) result.add(resultingSegment)
    } else {
      val startComparison = leftSegment.interval.compareStarts(rightSegment.interval)
      if (startComparison == -1) {
        remainingRightSegment = rightSegment
        val endComparison = leftSegment.interval.compareEndToStart(rightSegment.interval)
        if (endComparison < 1) {
          val resultingSegment = leftSegment.mapValue { op(it.value, null, it.interval) }.transpose()
          if (resultingSegment != null) result.add(resultingSegment)
        } else {
          remainingLeftSegment = leftSegment.mapInterval {
            Interval.between(
                rightSegment.interval.start,
                it.interval.end,
                rightSegment.interval.startInclusivity,
                it.interval.endInclusivity
            )
          }
          val resultingSegment = Segment(
              Interval.between(
                  leftSegment.interval.start,
                  rightSegment.interval.start,
                  leftSegment.interval.startInclusivity,
                  rightSegment.interval.startInclusivity.opposite()
              ),
              leftSegment.value
          ).mapValue { op(it.value, null, it.interval) }.transpose()
          if (resultingSegment != null) result.add(resultingSegment)
        }
      } else if (startComparison == 1) {
        remainingLeftSegment = leftSegment
        val endComparison = rightSegment.interval.compareEndToStart(leftSegment.interval)
        if (endComparison < 1) {
          val resultingSegment = rightSegment.mapValue { op(null, it.value, it.interval) }.transpose()
          if (resultingSegment != null) result.add(resultingSegment)
        } else {
          remainingRightSegment = rightSegment.mapInterval {
            Interval.between(
                leftSegment.interval.start,
                it.interval.end,
                leftSegment.interval.startInclusivity,
                it.interval.endInclusivity
            )
          }
          val resultingSegment = Segment(
              Interval.between(
                  rightSegment.interval.start,
                  leftSegment.interval.start,
                  rightSegment.interval.startInclusivity,
                  leftSegment.interval.startInclusivity.opposite()
              ),
              rightSegment.value
          ).mapValue { op(null, it.value, it.interval) }.transpose()
          if (resultingSegment != null) result.add(resultingSegment)
        }
      } else {
        val endComparison = leftSegment.interval.compareEnds(rightSegment.interval)
        if (endComparison == -1) {
          remainingRightSegment = rightSegment.mapInterval {
            Interval.between(
                leftSegment.interval.end,
                it.interval.end,
                leftSegment.interval.endInclusivity.opposite(),
                it.interval.endInclusivity
            )
          }
          val resultingSegment = leftSegment
              .mapValue { op(it.value, rightSegment.value, it.interval) }.transpose()
          if (resultingSegment != null) result.add(resultingSegment)
        } else if (endComparison == 1) {
          remainingLeftSegment = leftSegment.mapInterval {
            Interval.between(
                rightSegment.interval.end,
                it.interval.end,
                rightSegment.interval.endInclusivity.opposite(),
                it.interval.endInclusivity
            )
          }
          val resultingSegment = rightSegment
              .mapValue { op(leftSegment.value, it.value, it.interval) }.transpose()
          if (resultingSegment != null) result.add(resultingSegment)
        } else {
          val resultingSegment = leftSegment
              .mapValue { op(it.value, rightSegment.value, it.interval) }.transpose()
          if (resultingSegment != null) result.add(resultingSegment)
        }
      }
    }
  }

  return result
}
