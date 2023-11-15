package gov.nasa.jpl.aerie.timeline

fun interface Constructor<I, O> {
  fun construct(arg: I): O
}
