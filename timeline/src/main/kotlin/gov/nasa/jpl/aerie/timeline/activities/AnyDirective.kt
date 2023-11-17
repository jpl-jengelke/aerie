package gov.nasa.jpl.aerie.timeline.activities

import gov.nasa.jpl.aerie.merlin.protocol.types.SerializedValue

data class AnyDirective(val arguments: Map<String, SerializedValue>)
