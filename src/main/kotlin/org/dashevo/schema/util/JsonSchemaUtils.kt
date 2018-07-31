package org.dashevo.schema.util

import org.apache.commons.collections.CollectionUtils
import org.dashevo.schema.Object.DAPMETASCHEMA
import org.dashevo.schema.Object.DEFINITIONS
import org.dashevo.schema.Schema
import org.dashevo.schema.Validate
import org.dashevo.schema.model.Result
import org.dashevo.schema.model.Rules
import org.everit.json.schema.ValidationException
import org.json.JSONObject

object JsonSchemaUtils {

    /**
     * Validate a System Object or DAP Object.
     * DAP Object validation requires input of the DAP Schema.
     */
    fun validateSchemaObject(clonedObj: JSONObject, dapSchema: JSONObject? = null): Result {
        val validator = if (dapSchema != null) {
            Validate.createValidator(dapSchema)
        } else {
            Validate.createValidator(Schema.system)
        }

        val errors = arrayListOf<ValidationException>()
        try {
            validator.validate(clonedObj)
        } catch (e: ValidationException) {
            errors.addAll(e.causingExceptions)
        }

        val objType: String = if (dapSchema != null) {
            clonedObj.optString("objtype", "")
        } else {
            clonedObj.keys().next()
        }

        return convertValidationError(errors, objType)
    }

    /**
     * Convert ValidationError to Dash Schema Errors (Result) ?
     */
    fun convertValidationError(validationErrors: List<ValidationException>, objType: String): Result {
        // TODO: * ... catch more Validation Errors and tie to consensus rule errors
        if (CollectionUtils.isEmpty(validationErrors)) {
            return Result()
        }

        var code = 0
        var propName = ""
        val validationError = validationErrors[0]

        when (validationError.keyword) {
            "required" -> {
                code = Rules.DAPOBJECT_MISSING_PROPERTY.code
                propName = validationError.schemaLocation.split("/").last() //TODO: * Verify
            }
            "type" -> {
                code = Rules.DAPOBJECT_INVALID_TYPE.code
                propName = validationError.schemaLocation.split("/").last() //TODO: * Verify
            }

        }

        return Result(code, objType, propName, validationError.violatedSchema.title)
    }

    fun extractSchemaObject(clonedObj: JSONObject, dapSchema: JSONObject? = null): JSONObject {
        val validator = if (dapSchema != null) {
            Validate.createValidator(dapSchema, true)
        } else {
            Validate.createValidator(Schema.system, true)
        }

        val errors = arrayListOf<ValidationException>()
        try {
            validator.validate(clonedObj)
        } catch (e: ValidationException) {
            errors.addAll(e.causingExceptions)
        }

        if (CollectionUtils.isNotEmpty(errors)) {
            clonedObj.put("errors", errors) //TODO: * Check expected type of added errors
        }
        return clonedObj
    }

    fun validateDapSchemaDef(dapSchema: JSONObject): Result {
        try {
            Validate.createValidator(Schema.system)
            Validate.createValidator(dapSchema)
        } catch (e: ValidationException) {
            val result = Result(Rules.JSON_SCHEMA.code)
            result.errMsg = e.errorMessage
            return result
        }

        return Result()
    }

    fun validateDapSubschemaDef(dapSubschema: JSONObject): Result {
        val dapMetaSchema = Schema.system.getJSONObject("definitions").getJSONObject("dapmetaschema")
        val dapMetaSchemaValidator = Validate.createValidator(dapMetaSchema)
        val systemValidator = Validate.createValidator(Schema.system)

        try {
            dapMetaSchemaValidator.validate(dapSubschema)
            systemValidator.validate(dapSubschema)
        } catch (e: ValidationException) {
            val result = Result(Rules.JSON_SCHEMA.code)
            result.errMsg = e.errorMessage
            return result
        }
        return Result()
    }

    fun validateSchemaDef(schema: JSONObject): Result {
        try {
            Validate.createValidator(Schema.jsonSchema).validate(schema)
        } catch (e: ValidationException) {
            val result = Result(Rules.JSON_SCHEMA.code)
            result.errMsg = e.errorMessage
            return result
        }
        return Result()
    }

}