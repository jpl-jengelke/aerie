package gov.nasa.jpl.aerie.timeline

class Real(private val map: IntervalMap<Real>): IntervalMapOps<Real> by map {
  constructor(a: Int): this(IntervalMap(a, ::Real))
}
