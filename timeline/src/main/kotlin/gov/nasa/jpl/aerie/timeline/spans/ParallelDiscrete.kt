package gov.nasa.jpl.aerie.timeline.spans

import gov.nasa.jpl.aerie.timeline.Segment
import gov.nasa.jpl.aerie.timeline.Timeline
import gov.nasa.jpl.aerie.timeline.ops.TimelineOps
import gov.nasa.jpl.aerie.timeline.ops.BooleanOps
import gov.nasa.jpl.aerie.timeline.ops.ConstantOps
import gov.nasa.jpl.aerie.timeline.ops.ParallelOps

data class ParallelDiscrete<V: Any>(private val timeline: TimelineOps<Segment<V>, ParallelDiscrete<V>>):
    TimelineOps<Segment<V>, ParallelDiscrete<V>> by timeline,
    ParallelOps<Segment<V>, ParallelDiscrete<V>>,
    ConstantOps<V, ParallelDiscrete<V>>
{
  constructor(v: V): this(Timeline(::ParallelDiscrete) { bounds -> listOf(Segment(bounds, v)) })
}
