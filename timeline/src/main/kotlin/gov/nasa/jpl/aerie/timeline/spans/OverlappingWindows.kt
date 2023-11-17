package gov.nasa.jpl.aerie.timeline.spans

import gov.nasa.jpl.aerie.timeline.Segment
import gov.nasa.jpl.aerie.timeline.Timeline
import gov.nasa.jpl.aerie.timeline.ops.TimelineOps
import gov.nasa.jpl.aerie.timeline.ops.BooleanOps

data class OverlappingWindows(private val timeline: TimelineOps<Segment<Boolean>, OverlappingWindows>):
    TimelineOps<Segment<Boolean>, OverlappingWindows> by timeline,
    BooleanOps<OverlappingWindows>
{
  constructor(v: Boolean): this(Timeline(::OverlappingWindows) { bounds -> listOf(Segment(bounds, v)) })
}
