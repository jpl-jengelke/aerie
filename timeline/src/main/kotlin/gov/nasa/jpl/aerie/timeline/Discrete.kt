package gov.nasa.jpl.aerie.timeline

interface DiscreteOps<V: Any, P: Any>: ProfileOps<V, P> {
  fun all(f: (Segment<V>) -> Boolean, bounds: Interval) = collect(bounds).all(f)
}

data class Discrete<V: Any>(private val timeline: TimelineOps<Segment<V>, Discrete<V>>):
    TimelineOps<Segment<V>, Discrete<V>> by timeline,
    ProfileOps<V, Discrete<V>>,
    DiscreteOps<V, Discrete<V>>
{
  constructor(v: V): this(Timeline(::Discrete) { bounds -> listOf(Segment(bounds, v)) })
}
