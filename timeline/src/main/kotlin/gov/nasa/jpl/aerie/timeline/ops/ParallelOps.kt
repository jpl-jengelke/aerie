package gov.nasa.jpl.aerie.timeline.ops

import gov.nasa.jpl.aerie.timeline.IntervalLike
import gov.nasa.jpl.aerie.timeline.Segment

interface ParallelOps<T: IntervalLike<T>, S: Any>: TimelineOps<T, S> {
  fun <V: Any, SInto: Any> mapIntoSegments(ctor: (TimelineOps<Segment<V>, SInto>) -> SInto, f: (T) -> V) =
      mapInto(ctor) { Segment(it.interval, f(it)) }
  /*
  public flattenIntoProfile<Result>(map: (v: S) => Result, profileType: ProfileType): ProfileSpecialization<Result> {
    const flattenIntoProfileOp = (_: any, [$]: S[][]) => {
      const result = $.map(s => new Segment(map(s), s.interval));
      sortSegments(result, profileType);
      coalesce(result, profileType);
      return result;
    };
    const timeline = applyOperation(flattenIntoProfileOp, identityBoundsMap, this.spans);
    return new Profile(timeline, profileType).specialize();
  }

  public combineIntoProfile<Result>(
    op: BinaryOperation<Result, S, Result>,
    profileType: ProfileType
  ): ProfileSpecialization<Result> {
    const combineIntoProfileOp = ({ current: bounds }: { current: Interval }, [$]: S[][]) => {
      let acc: Segment<Result>[] = [];
      const remaining = $;
      while (remaining.length > 0) {
        const batch: Segment<S>[] = [];
        let previousTime = bounds.start;
        let previousInclusivity = Inclusivity.opposite(bounds.startInclusivity);
        for (const span of remaining) {
          const startComparison = Temporal.Duration.compare(span.interval.start, previousTime);
          if (
            startComparison > 0 ||
            (startComparison === 0 && previousInclusivity !== span.interval.startInclusivity)
          ) {
            batch.push(new Segment(span, span.interval));
            previousTime = span.interval.end;
            previousInclusivity = span.interval.endInclusivity;
          }
        }
        acc = map2Arrays(acc, batch, op);
      }
      return coalesce(acc, profileType);
    };
    const timeline = applyOperation(combineIntoProfileOp, identityBoundsMap, this.spans);
    return new Profile(timeline, profileType).specialize();
  }
   */

}
