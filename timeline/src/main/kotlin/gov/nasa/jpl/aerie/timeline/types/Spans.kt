package gov.nasa.jpl.aerie.timeline.types

import gov.nasa.jpl.aerie.timeline.IntervalLike
import gov.nasa.jpl.aerie.timeline.Timeline
import gov.nasa.jpl.aerie.timeline.ops.SpansOps
import gov.nasa.jpl.aerie.timeline.ops.TimelineOps

data class Spans<T: IntervalLike>(private val timeline: Timeline<T, Spans<T>>):
    TimelineOps<T, Spans<T>> by timeline,
    SpansOps<T, Spans<T>>
