package gov.nasa.jpl.aerie.timeline.ops.coalesce

import gov.nasa.jpl.aerie.merlin.protocol.types.Duration
import gov.nasa.jpl.aerie.merlin.protocol.types.RealDynamics
import gov.nasa.jpl.aerie.timeline.Segment
import gov.nasa.jpl.aerie.timeline.util.valueAt

interface CoalesceRealDynamics<P: Any>: Coalesce<RealDynamics, P> {
  override fun Segment<RealDynamics>.shouldCoalesce(other: Segment<RealDynamics>): Boolean {
    val t1 = this.interval.start
    val t2 = this.interval.start.plus(Duration.MINUTE)
    return (this.value.initial == other.valueAt(t1)
        && this.valueAt(t2) == other.valueAt(t2))
  }
}
