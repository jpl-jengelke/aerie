package gov.nasa.jpl.aerie.timeline

interface TimelineOps<V: IntervalLike, T: Any> {
  fun collect(bounds: Interval): List<V>
  fun specialize() = ctor(this)

  val ctor: (TimelineOps<V, T>) -> T
}

data class Timeline<V: IntervalLike, T: Any>(
    override val ctor: (TimelineOps<V, T>) -> T,
    private val collector: (Interval) -> List<V>
): TimelineOps<V, T> {
  override fun collect(bounds: Interval) = collector(bounds)
}
