package gov.nasa.jpl.aerie.timeline.types

import gov.nasa.jpl.aerie.timeline.Segment
import gov.nasa.jpl.aerie.timeline.Timeline
import gov.nasa.jpl.aerie.timeline.ops.DiscreteOps
import gov.nasa.jpl.aerie.timeline.ops.ProfileOps
import gov.nasa.jpl.aerie.timeline.ops.TimelineOps

data class Discrete<V: Any>(private val timeline: TimelineOps<Segment<V>, Discrete<V>>):
    TimelineOps<Segment<V>, Discrete<V>> by timeline,
    ProfileOps<V, Discrete<V>>,
    DiscreteOps<V, Discrete<V>>
{
  constructor(v: V): this(Timeline(::Discrete) { bounds -> listOf(Segment(bounds, v)) })
}
