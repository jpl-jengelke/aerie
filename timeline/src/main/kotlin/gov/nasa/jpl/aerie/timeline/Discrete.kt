package gov.nasa.jpl.aerie.timeline

class Discrete(private val map: IntervalMap<Discrete>): IntervalMapOps<Discrete> by map {
  constructor(a: Int): this(IntervalMap(a, ::Discrete))
}
