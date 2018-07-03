package org.dashevo.schema.model

data class ValidationResult(
        val valid: Boolean,
        val validateErrors: List<Error>? = null
)