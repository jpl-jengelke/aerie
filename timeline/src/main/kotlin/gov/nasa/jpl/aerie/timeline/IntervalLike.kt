package gov.nasa.jpl.aerie.timeline

interface IntervalLike<I> {
  val interval: Interval

  fun bound(bounds: Interval): I?
}
