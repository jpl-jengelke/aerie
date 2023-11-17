package gov.nasa.jpl.aerie.timeline

import gov.nasa.jpl.aerie.merlin.protocol.types.RealDynamics

class Real(private val map: Profile<RealDynamics, Real>): ProfileOps<RealDynamics, Real> by map {
  constructor(v: Int): this(Profile(::Real) { bounds -> listOf(Segment(bounds, RealDynamics.constant(v.toDouble())))} )
  constructor(v: Double): this(Profile(::Real) { bounds -> listOf(Segment(bounds, RealDynamics.constant(v)))} )
}
