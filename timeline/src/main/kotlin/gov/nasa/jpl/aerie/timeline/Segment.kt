package gov.nasa.jpl.aerie.timeline

data class Segment<V>(val interval: Interval, val value: V): IntervalLike {
  fun <W> mapValue(f: (Segment<V>) -> W) = Segment(interval, f(this))
  fun mapInterval(f: (Segment<V>) -> Interval) = Segment(f(this), value)

  fun bound(bounds: Interval): Segment<V>? {
    val intersection = bounds.intersection(this.interval)
    return if (intersection.isEmpty) null
    else Segment(intersection, this.value)
  }

  companion object {
    @JvmStatic fun <V> of(interval: Interval, value: V) = Segment(interval, value)
  }
}

fun <V> Segment<V?>.transpose() = if (value == null) null else Segment(interval, value)
