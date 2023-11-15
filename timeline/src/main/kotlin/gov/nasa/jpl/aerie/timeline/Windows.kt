package gov.nasa.jpl.aerie.timeline

class Windows(private val map: IntervalMap<Windows>): IntervalMapOps<Windows> by map {
  constructor(a: Int): this(IntervalMap(a, ::Windows))
}
