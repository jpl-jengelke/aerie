package gov.nasa.jpl.aerie.timeline.profiles

import gov.nasa.jpl.aerie.timeline.Segment
import gov.nasa.jpl.aerie.timeline.Timeline
import gov.nasa.jpl.aerie.timeline.ops.ConstantOps
import gov.nasa.jpl.aerie.timeline.ops.SerialOps
import gov.nasa.jpl.aerie.timeline.ops.TimelineOps
import gov.nasa.jpl.aerie.timeline.ops.coalesce.CoalesceNaive

data class Discrete<V: Any>(private val timeline: TimelineOps<Segment<V>, Discrete<V>>):
    TimelineOps<Segment<V>, Discrete<V>> by timeline,
    SerialOps<V, Discrete<V>>, CoalesceNaive<V, Discrete<V>>,
    ConstantOps<V, Discrete<V>>
{
  constructor(v: V): this(Timeline(::Discrete) { bounds -> listOf(Segment(bounds, v)) })
}
