package gov.nasa.jpl.aerie.timeline

fun interface Timeline<T: IntervalLike> {
  fun collect(bounds: Interval): List<T>
}
