package gov.nasa.jpl.aerie.timeline.ops

interface WindowsOps<P: Any>: ProfileOps<Boolean, P> {
  fun not() = mapValues { !it.value }
}
