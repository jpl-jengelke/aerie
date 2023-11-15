package gov.nasa.jpl.aerie.timeline

data class IntervalMap<T>(val a: Int, val c: Constructor<IntervalMap<T>, T>): IntervalMapOps<T> {
  override fun add(n: Int): T {
    return IntervalMap(this.a + n, this.c).wrap()
  }

  override fun map(f: (n: Int) -> Boolean) = IntervalMap(if (f(this.a)) 1 else 0, ::Windows).wrap()
  override fun map(f: (n: Int) -> Double) = IntervalMap(f(this.a).toInt(), ::Real).wrap()
  override fun <T> map(f: (n: Int) -> T) = IntervalMap(f(this.a).toString().length, ::Discrete).wrap()

  override fun print() {
    println("$a");
  }

  fun wrap(): T = c.construct(this)
}
