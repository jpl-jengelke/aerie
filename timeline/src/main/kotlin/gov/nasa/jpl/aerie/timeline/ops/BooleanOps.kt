package gov.nasa.jpl.aerie.timeline.ops

interface BooleanOps<P: Any>: ConstantOps<Boolean, P> {
  fun not() = mapValues { !it.value }
}
