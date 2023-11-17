package gov.nasa.jpl.aerie.timeline.profiles

import gov.nasa.jpl.aerie.timeline.Segment
import gov.nasa.jpl.aerie.timeline.Timeline
import gov.nasa.jpl.aerie.timeline.ops.TimelineOps
import gov.nasa.jpl.aerie.timeline.ops.BooleanOps
import gov.nasa.jpl.aerie.timeline.ops.SerialOps
import gov.nasa.jpl.aerie.timeline.ops.coalesce.CoalesceNaive

data class Windows(private val timeline: TimelineOps<Segment<Boolean>, Windows>):
    TimelineOps<Segment<Boolean>, Windows> by timeline,
    SerialOps<Boolean, Windows>,
    BooleanOps<Windows>,
    CoalesceNaive<Boolean, Windows>
{
  constructor(v: Boolean): this(Timeline(::Windows) { bounds -> listOf(Segment(bounds, v)) })
}
