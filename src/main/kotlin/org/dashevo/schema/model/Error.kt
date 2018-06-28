package org.dashevo.schema.model

import java.util.*

data class Error(
        val message: String,
        val type: String,
        val params: String,
        val dataPath: String,
        val schemaPath: String,
        val schemaName: String,
        val date: Date = Date()
)