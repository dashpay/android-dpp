package org.dashevo.schema.model

data class DapObject(
        val objtype: String,
        val idx: Int = 0,
        val rev: Int = 0,
        val act: Int = 0
)