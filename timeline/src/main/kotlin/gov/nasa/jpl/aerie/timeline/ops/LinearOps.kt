package gov.nasa.jpl.aerie.timeline.ops

import gov.nasa.jpl.aerie.merlin.protocol.types.RealDynamics
import gov.nasa.jpl.aerie.timeline.ops.coalesce.CoalesceRealDynamics

interface LinearOps<P: Any>: SegmentOps<RealDynamics, P>, CoalesceRealDynamics<P>
