package gov.nasa.jpl.aerie.timeline.profiles

import gov.nasa.jpl.aerie.timeline.Segment
import gov.nasa.jpl.aerie.timeline.Timeline
import gov.nasa.jpl.aerie.timeline.ops.SerialOps
import gov.nasa.jpl.aerie.timeline.ops.TimelineOps
import gov.nasa.jpl.aerie.timeline.ops.coalesce.CoalesceStrict

data class Profile<V : Any>(private val timeline: Timeline<Segment<V>, Profile<V>>):
    TimelineOps<Segment<V>, Profile<V>> by timeline,
    SerialOps<V, Profile<V>>, CoalesceStrict<V, Profile<V>>
