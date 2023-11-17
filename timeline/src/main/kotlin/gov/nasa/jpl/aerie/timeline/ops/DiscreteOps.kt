package gov.nasa.jpl.aerie.timeline.ops

import gov.nasa.jpl.aerie.timeline.Interval
import gov.nasa.jpl.aerie.timeline.Segment

interface DiscreteOps<V: Any, P: Any>: ProfileOps<V, P> {
  fun all(f: (Segment<V>) -> Boolean, bounds: Interval) = collect(bounds).all(f)
}
