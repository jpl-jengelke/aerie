package gov.nasa.jpl.aerie.timeline

import gov.nasa.jpl.aerie.merlin.protocol.types.RealDynamics

class Real(private val map: IntervalMap<RealDynamics, Real>): IntervalMapOps<RealDynamics, Real> by map {
  constructor(v: Int): this(IntervalMap(::Real) { bounds -> listOf(Segment(bounds, RealDynamics.constant(v.toDouble())))} )
  constructor(v: Double): this(IntervalMap(::Real) { bounds -> listOf(Segment(bounds, RealDynamics.constant(v)))} )
}
