package org.dashevo.schema.util

import org.apache.commons.collections.CollectionUtils
import org.dashevo.schema.Definition
import org.dashevo.schema.Schema
import org.dashevo.schema.Validate
import org.dashevo.schema.model.Result
import org.dashevo.schema.model.Rules
import org.everit.json.schema.ValidationException
import org.json.JSONObject

object JsonSchemaUtils {

    fun validateSchemaObject(clonedObj: JSONObject, dapSchema: JSONObject?): Result {

        val validators = arrayListOf(Validate.schemaValidator)
        if (dapSchema != null) {
            val dapSubSchema = Definition.getDapSubSchema(clonedObj, dapSchema)
            if (dapSubSchema != null) {
                validators.add(Validate.createValidator(dapSubSchema))
            }
        }

        val validationErrors = arrayListOf<ValidationException>()
        validators.forEach { validator ->
            try {
                validator.validate(clonedObj)
            } catch (e: ValidationException) {
                validationErrors.add(e)
            }
        }

        val objType: String
        val schema: JSONObject
        if (dapSchema != null) {
            objType = clonedObj.getString("objtype")
            schema = dapSchema
        } else {
            objType = clonedObj.keys().next()
            schema = Schema.system
        }

        return convertValidationError(validationErrors, objType, schema)
    }

    /**
     * Convert ValidationError to Dash Schema Errors (Result) ?
     */
    private fun convertValidationError(validationErrors: ArrayList<ValidationException>, objType: String,
                                       schema: JSONObject): Result {
        if (CollectionUtils.isNotEmpty(validationErrors)) {
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

    fun extractSchemaObject(objCopy: JSONObject, dapSchema: JSONObject?): JSONObject {
        val validators = arrayListOf(Validate.schemaValidator)
        if (dapSchema != null) {
            validators.add(Validate.createValidator(dapSchema))
        }

        val validationErrors = arrayListOf<ValidationException>()
        validators.forEach { validator ->
            try {
                validator.validate(objCopy)
            } catch (e: ValidationException) {
                validationErrors.add(e)
            }
        }

        //TODO: remove non-schema properties using the validation errors as source of the filter

        if (CollectionUtils.isNotEmpty(validationErrors)) {
            objCopy.put("errors", validationErrors) //TODO: Check expected type of added errors
        }

        return objCopy
    }

    fun validateDapSchemaDef(dapSchema: JSONObject): Result {
        try {
            Validate.createValidator(Schema.system).validate(dapSchema)
        } catch (e: ValidationException) {
            return Result(0) //TODO (?)
        }

        return Result()
    }

    fun validateSysSchemaDef(sysSchema: JSONObject): Result {
        try {
            Validate.schemaValidator.validate(sysSchema)
        } catch (e: ValidationException) {
            return Result(0) //TODO (?)
        }

        return Result()
    }

}