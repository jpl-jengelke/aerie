package gov.nasa.jpl.aerie.timeline.profiles

import gov.nasa.jpl.aerie.merlin.protocol.types.RealDynamics
import gov.nasa.jpl.aerie.timeline.Segment
import gov.nasa.jpl.aerie.timeline.Timeline
import gov.nasa.jpl.aerie.timeline.ops.ConstantOps
import gov.nasa.jpl.aerie.timeline.ops.LinearOps
import gov.nasa.jpl.aerie.timeline.ops.SerialOps
import gov.nasa.jpl.aerie.timeline.ops.TimelineOps
import gov.nasa.jpl.aerie.timeline.ops.coalesce.CoalesceRealDynamics

data class Real(private val timeline: TimelineOps<Segment<RealDynamics>, Real>):
    TimelineOps<Segment<RealDynamics>, Real> by timeline,
    SerialOps<RealDynamics, Real>,
    LinearOps<Real>,
    CoalesceRealDynamics<Real>
{
  constructor(v: Int): this(Timeline(::Real) { bounds -> listOf(Segment(bounds, RealDynamics.constant(v.toDouble())))} )
  constructor(v: Double): this(Timeline(::Real) { bounds -> listOf(Segment(bounds, RealDynamics.constant(v)))} )
}
