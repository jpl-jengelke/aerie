package gov.nasa.jpl.aerie.timeline.spans

import gov.nasa.jpl.aerie.timeline.Segment
import gov.nasa.jpl.aerie.timeline.Timeline
import gov.nasa.jpl.aerie.timeline.ops.TimelineOps
import gov.nasa.jpl.aerie.timeline.ops.BooleanOps
import gov.nasa.jpl.aerie.timeline.ops.ParallelOps

data class ParallelWindows(private val timeline: TimelineOps<Segment<Boolean>, ParallelWindows>):
    TimelineOps<Segment<Boolean>, ParallelWindows> by timeline,
    ParallelOps<Segment<Boolean>, ParallelWindows>,
    BooleanOps<ParallelWindows>
{
  constructor(v: Boolean): this(Timeline(::ParallelWindows) { bounds -> listOf(Segment(bounds, v)) })
}
