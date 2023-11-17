package gov.nasa.jpl.aerie.timeline

interface DiscreteOps<V: Any, P>: ProfileOps<V, P> {
  fun all(f: (Segment<V>) -> Boolean, bounds: Interval) = collect(bounds).all(f)
}

class Discrete<V: Any>(private val map: Profile<V, Discrete<V>>): ProfileOps<V, Discrete<V>> by map, DiscreteOps<V, Discrete<V>> {
  constructor(v: V): this(Profile(::Discrete) { bounds -> listOf(Segment(bounds, v)) })
}
