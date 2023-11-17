package gov.nasa.jpl.aerie.timeline

import gov.nasa.jpl.aerie.timeline.ops.ProfileOps
import gov.nasa.jpl.aerie.timeline.ops.TimelineOps

data class Profile<V : Any>(private val timeline: Timeline<Segment<V>, Profile<V>>):
    TimelineOps<Segment<V>, Profile<V>> by timeline,
    ProfileOps<V, Profile<V>>
