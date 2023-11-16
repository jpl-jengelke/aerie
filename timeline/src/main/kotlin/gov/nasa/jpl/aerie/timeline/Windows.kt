package gov.nasa.jpl.aerie.timeline

class Windows(private val map: IntervalMap<Boolean, Windows>): IntervalMapOps<Boolean, Windows> by map {
  constructor(b: Boolean): this(IntervalMap(::Windows) { bounds -> listOf(Segment(bounds, b)) })

  fun not() = mapValues { !it.value }
}
