package gov.nasa.jpl.aerie.timeline

open class Discrete<V : Any>(private val map: IntervalMap<V, Discrete<V>>): IntervalMapOps<V, Discrete<V>> by map {
  constructor(v: V): this(IntervalMap(::Discrete) { bounds -> listOf(Segment(bounds, v)) })
}
