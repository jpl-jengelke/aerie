package gov.nasa.jpl.aerie.timeline

interface IntervalMapOps<T> {
  fun add(n: Int): T
  fun map(f: (n: Int) -> Boolean): Windows
  fun map(f: (n: Int) -> Double): Real
  fun <T> map(f: (n: Int) -> T): Discrete
  fun print()
}
