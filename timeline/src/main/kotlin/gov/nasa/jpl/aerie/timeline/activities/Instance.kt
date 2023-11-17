package gov.nasa.jpl.aerie.timeline.activities

import gov.nasa.jpl.aerie.timeline.Interval
import gov.nasa.jpl.aerie.timeline.IntervalLike

data class Instance<A>(
    val instance: A,
    val type: String,
    val directiveId: Int,
    override val interval: Interval,
): IntervalLike<Instance<A>> {
  override fun bound(bounds: Interval): Instance<A>? {
    TODO("Not yet implemented")
  }
}
