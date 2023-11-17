package gov.nasa.jpl.aerie.timeline.activities

import gov.nasa.jpl.aerie.merlin.protocol.types.Duration
import gov.nasa.jpl.aerie.timeline.Interval
import gov.nasa.jpl.aerie.timeline.IntervalLike

data class Directive<A>(
    val directive: A,
    val startTime: Duration
): IntervalLike<Directive<A>> {
  override val interval: Interval
    get() = Interval.at(startTime)

  override fun bound(bounds: Interval): Directive<A>? {
    TODO("Not yet implemented")
  }
}
