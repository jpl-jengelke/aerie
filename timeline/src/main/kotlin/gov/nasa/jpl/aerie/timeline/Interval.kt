package gov.nasa.jpl.aerie.timeline

import gov.nasa.jpl.aerie.merlin.protocol.types.Duration

/**
 * An Interval on the timeline, represented by start and end points
 * and start and end inclusivity.
 */
data class Interval(
    val start: Duration,
    val end: Duration,
    val startInclusivity: Inclusivity = Inclusivity.Inclusive,
    val endInclusivity: Inclusivity = startInclusivity
): Comparable<Interval?> {

  constructor(start: Duration, end: Duration) : this(start, end, Inclusivity.Inclusive, Inclusivity.Inclusive)

  enum class Inclusivity {
    Inclusive,
    Exclusive;

    fun opposite(): Inclusivity = if ((this == Inclusive)) Exclusive else Inclusive
    fun moreRestrictiveThan(other: Inclusivity): Boolean = this == Exclusive && other == Inclusive
  }

  fun includesStart() = startInclusivity == Inclusivity.Inclusive
  fun includesEnd() = endInclusivity == Inclusivity.Inclusive

  val isEmpty: Boolean
    get() {
      if (end.shorterThan(start)) return true
      if (end.longerThan(start)) return false
      return !(includesStart() && includesEnd())
    }

  val isPoint: Boolean
    // Use this instead of `.duration().isZero()` to avoid overflow on long intervals.
    get() = (includesStart() && includesEnd() && (start === end))

  fun shiftBy(duration: Duration) = between(
      start.saturatingPlus(duration),
      end.saturatingPlus(duration),
      startInclusivity,
      endInclusivity
  )

  fun duration() = if (isEmpty) Duration.ZERO else end.minus(start)

  fun isStrictlyAfter(x: Interval) = compareStartToEnd(x) > 0
  fun isStrictlyBefore(x: Interval) = compareEndToStart(x) < 0

  fun compareStarts(other: Interval): Int {
    val timeComparison: Int = start.compareTo(other.start)
    return if (timeComparison != 0) timeComparison
      else if (startInclusivity == other.startInclusivity) 0
      else if (startInclusivity == Inclusivity.Inclusive) -1
      else 1
  }
  fun compareEnds(other: Interval): Int {
    val timeComparison: Int = end.compareTo(other.end)
    return if (timeComparison != 0) timeComparison
      else if (startInclusivity == other.startInclusivity) 0
      else if (startInclusivity == Inclusivity.Inclusive) 1
      else -1
  }

  fun compareStartToEnd(other: Interval) = -other.compareEndToStart(this)

  /**
   * Compares the end of x to the start of y.
   *
   * Returns -1 if x ends before y starts.
   * Returns 1 if x ends after (see below) y starts.
   * Returns 0 if x exactly meets (see below) y with no overlap
   *
   * To clarify, `compareEndToStart([a, b), [b, c)) == 0`,
   * but `compareEndToStart([a, b], [b, c]) == 1`. This might be unintuitive,
   * but I've found this to be much more useful in practice than `-1` and `0`, respectively.
   * This is because as long as x starts before y, we get a few properties:
   * - -1 indicates a gap between x and y
   * - 1 indicates overlap between x and y
   * - 0 indicates no gap and no overlap
   */
  fun compareEndToStart(other: Interval): Int {
    val timeComparison: Int = this.end.compareTo(other.start)
    return if (timeComparison != 0) timeComparison
      else if (this.endInclusivity != other.startInclusivity) 0
      else if (this.endInclusivity == Inclusivity.Inclusive) 1
      else -1
  }

  fun compareStartToStart(other: Interval): Int {
    // First, order by absolute time.
    if (!this.start.isEqualTo(other.start)) {
      return this.start.compareTo(other.start)
    }

    // Second, order by whichever one includes the point.
    if (this.includesStart() != other.includesStart()) {
      return if ((this.includesStart())) -1 else 1
    }
    return 0
  }

  fun compareEndToEnd(other: Interval): Int {
    // First, order by absolute time.
    if (!this.end.isEqualTo(other.end)) {
      return this.end.compareTo(other.end)
    }

    // Second, order by whichever one includes the point
    if (this.includesEnd() != other.includesEnd()) {
      return if ((this.includesEnd())) 1 else -1
    }
    return 0
  }

  fun hasSameStart(other: Interval) = compareStartToStart(other) == 0
  fun hasSameEnd(other: Interval) = compareEndToEnd(other) == 0

  fun meets(other: Interval) = (this.end.isEqualTo(other.start)) && (this.endInclusivity != other.startInclusivity)
  fun metBy(other: Interval) = other.meets(this)

  operator fun contains(d: Duration) = !intersection(at(d)).isEmpty
  operator fun contains(x: Interval) = intersection(x) == x

  fun adjacent(x: Interval) = metBy(x) || meets(x)

  fun intersection(other: Interval): Interval {
    val start: Duration
    val startInclusivity: Inclusivity
    if (this.start.longerThan(other.start)) {
      start = this.start
      startInclusivity = this.startInclusivity
    } else if (other.start.longerThan(this.start)) {
      start = other.start
      startInclusivity = other.startInclusivity
    } else {
      start = this.start
      startInclusivity = if (this.includesStart() && other.includesStart()) Inclusivity.Inclusive else Inclusivity.Exclusive
    }
    val end: Duration
    val endInclusivity: Inclusivity
    if (this.end.shorterThan(other.end)) {
      end = this.end
      endInclusivity = this.endInclusivity
    } else if (other.end.shorterThan(this.end)) {
      end = other.end
      endInclusivity = other.endInclusivity
    } else {
      end = this.end
      endInclusivity = if ((this.includesEnd() && other.includesEnd())) Inclusivity.Inclusive else Inclusivity.Exclusive
    }
    return between(start, end, startInclusivity, endInclusivity)
  }

  fun union(other: Interval): Interval {
    if (this.isEmpty) return this
    if (other.isEmpty) return other
    val start: Duration
    val startInclusivity: Inclusivity
    if (this.start.shorterThan(other.start)) {
      start = this.start
      startInclusivity = this.startInclusivity
    } else if (other.start.shorterThan(this.start)) {
      start = other.start
      startInclusivity = other.startInclusivity
    } else {
      start = this.start
      startInclusivity = if ((this.includesStart() || other.includesStart())) Inclusivity.Inclusive else Inclusivity.Exclusive
    }
    val end: Duration
    val endInclusivity: Inclusivity
    if (this.end.longerThan(other.end)) {
      end = this.end
      endInclusivity = this.endInclusivity
    } else if (other.end.longerThan(this.end)) {
      end = other.end
      endInclusivity = other.endInclusivity
    } else {
      end = this.end
      endInclusivity = if ((this.includesEnd() || other.includesEnd())) Inclusivity.Inclusive else Inclusivity.Exclusive
    }
    return between(start, end, startInclusivity, endInclusivity)
  }

  override operator fun compareTo(other: Interval?): Int {
    return start.compareTo(other?.start)
  }

  override fun toString(): String {
    return if (isEmpty) {
      "(empty)"
    } else {
      "${if (includesStart()) "[" else "("}$start, $end${if (includesEnd()) "]" else ")"}"
    }
  }

  companion object {
    /**
     * Constructs an interval between two durations based on a common instant.
     *
     * @param start The starting time of the interval.
     * @param end The ending time of the interval.
     * @return A non-empty interval if start < end, or an empty interval otherwise.
     */
    @JvmStatic fun between(
        start: Duration,
        end: Duration,
        startInclusivity: Inclusivity = Inclusivity.Inclusive,
        endInclusivity: Inclusivity = startInclusivity
    ) = if (end.shorterThan(start)) EMPTY else Interval(start, end, startInclusivity, endInclusivity)
    @JvmStatic fun between(
        start: Long,
        end: Long,
        startInclusivity: Inclusivity = Inclusivity.Inclusive,
        endInclusivity: Inclusivity = startInclusivity,
        unit: Duration
    ) = between(Duration.of(start, unit), Duration.of(end, unit), startInclusivity, endInclusivity)

    @JvmStatic fun betweenClosedOpen(start: Duration, end: Duration) = between(start, end, Inclusivity.Inclusive, Inclusivity.Exclusive)

    @JvmStatic fun at(point: Duration) = between(point, point)
    @JvmStatic fun at(quantity: Long, unit: Duration) = at(Duration.of(quantity, unit))

    @JvmStatic val EMPTY = Interval(Duration.ZERO, Duration.ZERO.minus(Duration.EPSILON))
  }
}
