package gov.nasa.jpl.aerie.timeline.util

import gov.nasa.jpl.aerie.merlin.protocol.types.Duration
import gov.nasa.jpl.aerie.merlin.protocol.types.RealDynamics
import gov.nasa.jpl.aerie.timeline.Segment

fun Segment<RealDynamics>.valueAt(time: Duration) =
    value.initial + value.rate * (time.minus(interval.start).ratioOver(Duration.SECOND))
