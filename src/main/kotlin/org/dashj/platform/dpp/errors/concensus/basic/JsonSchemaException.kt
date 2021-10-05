package org.dashj.platform.dpp.errors.concensus.basic

import org.dashj.platform.dpp.errors.concensus.ConcensusException

class JsonSchemaException(
    message: String,
    val keyword: String,
    val instancePath: String,
    val schemaPath: String,
    val params: Map<String, Any>,
    val propertyName: String
) : ConcensusException(message) {
    constructor(arguments: List<Any>) : this(
        arguments[0] as String, arguments[1] as String, arguments[2] as String,
        arguments[3] as String, arguments[4] as Map<String, Any>, arguments[5] as String
    ) {
        setArguments(arguments)
    }
}
