package gov.nasa.jpl.aerie.timeline

interface WindowsOps<P: Any>: ProfileOps<Boolean, P> {
  fun not() = mapValues { !it.value }
}

data class Windows(private val timeline: TimelineOps<Segment<Boolean>, Windows>):
    TimelineOps<Segment<Boolean>, Windows> by timeline,
    ProfileOps<Boolean, Windows>,
    DiscreteOps<Boolean, Windows>,
    WindowsOps<Windows>
{
  constructor(v: Boolean): this(Timeline(::Windows) { bounds -> listOf(Segment(bounds, v)) })
}
