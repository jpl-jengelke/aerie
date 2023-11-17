package gov.nasa.jpl.aerie.timeline.util

import gov.nasa.jpl.aerie.timeline.Segment

fun <V> coalesce(segments: List<Segment<V>>) {

}

/*
export function sortSegments<V>(segments: Segment<V>[], profileType: ProfileType): Segment<V>[] {
  const valueComparator = ProfileType.getSegmentComparator(profileType);
  return segments.sort((l: Segment<any>, r: Segment<any>) => {
    const startComparison = Interval.compareStarts(l.interval, r.interval);
    const endComparison = Interval.compareEnds(l.interval, r.interval);
    if (startComparison === endComparison && startComparison !== 0) {
      return startComparison;
    } else {
      if (valueComparator(l.value, r.value)) return startComparison;
      throw new Error(
        'Segments should be sortable into an order in which both start and end times are strictly increasing, unless segment values are equal.'
      );
    }
  });
}

/**
 * In-place flattens an array of overlapping segments into non-overlapping segments with unequal consecutive values.
 *
 * *Input condition*: segments must be sorted such that between each pair of consecutive elements, one of the following is true:
 * - if the values are unequal, the start and end times (including inclusivity) must be strictly increasing
 * - if the values are equal, the start time must be non-decreasing.
 *
 * This input condition is not checked, and violating it is undefined behavior.
 *
 * Empty intervals are removed, and their values are not considered for the purposes of the sorted input condition.
 *
 * @param segments
 * @param typeTag
 */
export function coalesce<V>(segments: Segment<V>[], typeTag: ProfileType): Segment<V>[] {
  const equals = ProfileType.getSegmentComparator(typeTag);
  if (segments.length === 0) return segments;
  let shortIndex = 0;
  let startIndex = 0;
  while (segments[startIndex].interval.isEmpty()) startIndex++;
  let buffer = segments[startIndex];
  for (const segment of segments.slice(startIndex + 1)) {
    if (segment.interval.isEmpty()) continue;
    const comparison = Interval.compareEndToStart(buffer.interval, segment.interval);
    if (comparison === -1) {
      segments[shortIndex++] = buffer;
      buffer = segment;
    } else if (comparison === 0) {
      if (equals(buffer.value, segment.value)) {
        if (Interval.compareEnds(buffer.interval, segment.interval) < 0) {
          buffer.interval.end = segment.interval.end;
          buffer.interval.endInclusivity = segment.interval.endInclusivity;
        }
      } else {
        segments[shortIndex++] = buffer;
        buffer = segment;
      }
    } else {
      if (equals(buffer.value, segment.value)) {
        if (Interval.compareEnds(buffer.interval, segment.interval) < 0) {
          buffer.interval.end = segment.interval.end;
          buffer.interval.endInclusivity = segment.interval.endInclusivity;
        }
      } else {
        buffer.interval.end = segment.interval.start;
        buffer.interval.endInclusivity = Inclusivity.opposite(segment.interval.startInclusivity);
        segments[shortIndex++] = buffer;
        buffer = segment;
      }
    }
  }
  segments[shortIndex++] = buffer;
  segments.splice(shortIndex);
  return segments;
}
 */
