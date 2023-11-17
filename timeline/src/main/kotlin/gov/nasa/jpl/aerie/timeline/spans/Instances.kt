package gov.nasa.jpl.aerie.timeline.spans

import gov.nasa.jpl.aerie.timeline.Timeline
import gov.nasa.jpl.aerie.timeline.activities.Instance
import gov.nasa.jpl.aerie.timeline.ops.*

data class Instances<A: Any>(private val timeline: TimelineOps<Instance<A>, Instances<A>>):
    TimelineOps<Instance<A>, Instances<A>> by timeline,
    ParallelOps<Instance<A>, Instances<A>>,
    ActivityInstanceOps
{
  constructor(i: Instance<A>): this(Timeline(::Instances) { bounds ->
    val bounded = i.bound(bounds)
    if (bounded != null) listOf(bounded)
    else listOf<Instance<A>>()
  })
}
