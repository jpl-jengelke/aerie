package gov.nasa.jpl.aerie.timeline

class Windows(private val map: Profile<Boolean, Windows>): DiscreteOps<Boolean, Windows>, ProfileOps<Boolean, Windows> by map {
  constructor(b: Boolean): this(Profile(::Windows) { bounds -> listOf(Segment(bounds, b)) })

  fun not() = mapValues { !it.value }
}
