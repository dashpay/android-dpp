package org.dashj.platform.dpp.validation

class JsonSchemaValidator : Validator {

    enum class SCHEMAS(val value: String) {
        DATA_CONTRACT("https://schema.dash.org/dpp-0-4-0/meta/data-contract"),
        DP_OBJECT("https://schema.dash.org/dpp-0-4-0/base/document")
    }

    /**
     * @param {object} schema
     * @param {object} object
     * @param {array|Object} additionalSchemas
     * @return {ValidationResult}
     */
    fun validate(
        schema: MutableMap<String, Any>,
        obj: Any,
        additionalSchemas: List<MutableMap<String, Any>>
    ): ValidationResult {
        TODO()
    }

    /**
     * Validate JSON Schema
     *
     * @param {object} schema
     * @param additionalSchemas
     * @return {ValidationResult}
     */
    fun validateSchema(
        schema: MutableMap<String, Any>,
        additionalSchemas: List<MutableMap<String, Any>>
    ): ValidationResult {
        TODO()
    }
}
