package gov.nasa.jpl.aerie.timeline.ops

import gov.nasa.jpl.aerie.timeline.*
import gov.nasa.jpl.aerie.timeline.ops.coalesce.Coalesce
import gov.nasa.jpl.aerie.timeline.util.map2

interface SegmentOps<V : Any, P: Any>: TimelineOps<Segment<V>, P> {
  fun mapValues(f: (Segment<V>) -> V) = mapValuesInto(ctor, f)
  fun <W: Any, PInto: Any> mapValuesInto(ctor: (TimelineOps<Segment<W>, PInto>) -> PInto, f: (Segment<V>) -> W) =
      Timeline(ctor) { bounds -> collect(bounds).map { it.mapValue(f) }}.specialize()
}
