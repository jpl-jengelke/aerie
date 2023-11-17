package gov.nasa.jpl.aerie.timeline.spans

import gov.nasa.jpl.aerie.merlin.protocol.types.RealDynamics
import gov.nasa.jpl.aerie.timeline.Segment
import gov.nasa.jpl.aerie.timeline.Timeline
import gov.nasa.jpl.aerie.timeline.ops.*

data class ParallelReal(private val timeline: TimelineOps<Segment<RealDynamics>, ParallelReal>):
    TimelineOps<Segment<RealDynamics>, ParallelReal> by timeline,
    ParallelOps<Segment<RealDynamics>, ParallelReal>,
    LinearOps<ParallelReal>
{
  constructor(v: RealDynamics): this(Timeline(::ParallelReal) { bounds -> listOf(Segment(bounds, v)) })
}
