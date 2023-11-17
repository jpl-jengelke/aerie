package gov.nasa.jpl.aerie.timeline

import gov.nasa.jpl.aerie.timeline.ops.DiscreteOps
import gov.nasa.jpl.aerie.timeline.ops.ProfileOps
import gov.nasa.jpl.aerie.timeline.ops.TimelineOps
import gov.nasa.jpl.aerie.timeline.ops.WindowsOps

data class Windows(private val timeline: TimelineOps<Segment<Boolean>, Windows>):
    TimelineOps<Segment<Boolean>, Windows> by timeline,
    ProfileOps<Boolean, Windows>,
    DiscreteOps<Boolean, Windows>,
    WindowsOps<Windows>
{
  constructor(v: Boolean): this(Timeline(::Windows) { bounds -> listOf(Segment(bounds, v)) })
}
