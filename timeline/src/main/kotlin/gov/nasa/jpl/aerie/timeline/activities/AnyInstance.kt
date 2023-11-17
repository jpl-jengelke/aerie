package gov.nasa.jpl.aerie.timeline.activities

import gov.nasa.jpl.aerie.merlin.protocol.types.SerializedValue

data class AnyInstance(
    val arguments: Map<String, SerializedValue>,
    val computedAttributes: Map<String, SerializedValue>
)
