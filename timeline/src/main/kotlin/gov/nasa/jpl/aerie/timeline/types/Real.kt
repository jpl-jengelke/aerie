package gov.nasa.jpl.aerie.timeline.types

import gov.nasa.jpl.aerie.merlin.protocol.types.RealDynamics
import gov.nasa.jpl.aerie.timeline.Segment
import gov.nasa.jpl.aerie.timeline.Timeline
import gov.nasa.jpl.aerie.timeline.ops.ProfileOps
import gov.nasa.jpl.aerie.timeline.ops.TimelineOps

data class Real(private val timeline: TimelineOps<Segment<RealDynamics>, Real>):
    TimelineOps<Segment<RealDynamics>, Real> by timeline,
    ProfileOps<RealDynamics, Real>
{
  constructor(v: Int): this(Timeline(::Real) { bounds -> listOf(Segment(bounds, RealDynamics.constant(v.toDouble())))} )
  constructor(v: Double): this(Timeline(::Real) { bounds -> listOf(Segment(bounds, RealDynamics.constant(v)))} )
}
