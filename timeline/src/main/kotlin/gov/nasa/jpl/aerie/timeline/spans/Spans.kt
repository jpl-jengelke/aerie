package gov.nasa.jpl.aerie.timeline.spans

import gov.nasa.jpl.aerie.timeline.IntervalLike
import gov.nasa.jpl.aerie.timeline.Timeline
import gov.nasa.jpl.aerie.timeline.ops.ParallelOps
import gov.nasa.jpl.aerie.timeline.ops.TimelineOps

data class Spans<T: IntervalLike>(private val timeline: Timeline<T, Spans<T>>):
    TimelineOps<T, Spans<T>> by timeline,
    ParallelOps<T, Spans<T>>
