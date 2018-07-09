package org.dashevo.schema.util

import org.apache.commons.collections.CollectionUtils
import org.dashevo.schema.Schema
import org.dashevo.schema.Validate
import org.dashevo.schema.model.Result
import org.dashevo.schema.model.Rules
import org.everit.json.schema.ValidationException
import org.json.JSONObject

object JsonSchemaUtils {

    fun validateSchemaObject(clonedObj: JSONObject, dapSchema: JSONObject?): Result {
        val validator = if (dapSchema != null) {
            Validate.createValidator(dapSchema)
        } else {
            Validate.systemSchemaValidator
        }

        val errors = arrayListOf<ValidationException>()
        try {
            validator.validate(clonedObj)
        } catch (e: ValidationException) {
            errors.addAll(e.causingExceptions)
        }

        val objType: String = if (dapSchema != null) {
            clonedObj.getString("objtype")
        } else {
            clonedObj.keys().next()
        }

        return convertValidationError(listOf(), objType)
    }

    /**
     * Convert ValidationError to Dash Schema Errors (Result) ?
     */
    private fun convertValidationError(validationErrors: List<ValidationException>, objType: String): Result {
        if (CollectionUtils.isEmpty(validationErrors)) {
            return Result()
        }

        var code = 0
        var propName = ""
        val validationError = validationErrors[0]

        when (validationError.keyword) {
            "required" -> {
                code = Rules.DAPOBJECT_MISSING_PROPERTY.code
                propName = validationError.schemaLocation.split("/").last() //TODO: Verify
            }
            "type" -> {
                code = Rules.DAPOBJECT_INVALID_TYPE.code
                propName = validationError.schemaLocation.split("/").last() //TODO: Verify
            }

        }

        return Result(code, objType, propName, validationError.violatedSchema.title)
    }

    fun extractSchemaObject(clonedObj: JSONObject, dapSchema: JSONObject? = null): JSONObject {
        val validator = if (dapSchema != null) {
            Validate.createValidator(dapSchema, true)
        } else {
            Validate.systemSchemaValidator
        }

        val errors = arrayListOf<ValidationException>()
        try {
            validator.validate(clonedObj)
        } catch (e: ValidationException) {
            errors.addAll(e.causingExceptions)
        }

        //TODO: remove non-schema properties using the validation errors as source of the filter

        if (CollectionUtils.isNotEmpty(errors)) {
            clonedObj.put("errors", errors) //TODO: Check expected type of added errors
        }

        return clonedObj
    }

    fun validateDapSchemaDef(dapSchema: JSONObject): Result {
        try {
            Validate.systemSchemaValidator.validate(dapSchema)
        } catch (e: ValidationException) {
            return Result(0) //TODO (?)
        }

        return Result()
    }

    fun validateSysSchemaDef(sysSchema: JSONObject): Result {
        try {
            Validate.systemSchemaValidator.validate(sysSchema)
        } catch (e: ValidationException) {
            return Result(0) //TODO (?)
        }

        return Result()
    }

}