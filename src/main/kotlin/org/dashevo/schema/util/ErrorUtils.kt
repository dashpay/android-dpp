package org.dashevo.schema.util

import org.dashevo.schema.model.Error
import org.dashevo.schema.model.ValidationResult
import org.everit.json.schema.ValidationException

object ErrorUtils {

    private const val TYPE_ERROR = 1
    private const val REQUIRED_ERROR = 2
    private const val UNKNOWN_ERROR = 999999

    /**
     * Create a new Dash Schema error
     * @param message Message
     * @param type Type
     * @param params Params
     * @param dataPath Data path
     * @param schemaPath Schema path
     * @param schemaName Schema name
     * @returns {{date: number, type: string, params: string, dataPath: string, schemaPath: string, schemaName: string, message: string}}
     */
    //TODO: Consider removing this method
    fun newError(
            message: String,
            type: String = "",
            params: String = "",
            dataPath: String = "",
            schemaPath: String = "",
            schemaName: String = ""
    ): Error {
        return Error(message, type, params, dataPath, schemaPath, schemaName)
    }

    /**
     * Returns a new Dash Schema error
     * @param message Message
     * @param type Type
     * @param params Params
     * @param dataPath Data path
     * @param schemaPath Schema path
     * @param schemaName Schema name
     * @returns {{valid, validateErrors}}
     */
    fun result(
            message: String = "",
            type: String = "",
            params: String = "",
            dataPath: String = "",
            schemaPath: String = "",
            schemaName: String = ""
    ): ValidationResult {
        val error = Error(message, type, params, dataPath, schemaPath, schemaName)
        return ValidationResultUtils.result(error)
    }

    /**
     * Convert SchemaValidator errors to Dash Schema errors
     * @param inpErrors Array of Ajv errors
     * @param schemaName Name of the Schema definition being validated
     * @returns {*}
     */
    fun convertValidationErrors(inpErrors: List<ValidationException>): List<Error> {
        val outErrors = arrayListOf<Error>()
        inpErrors.forEach { error ->

            val errorType = when (error.keyword) {
                "type" -> TYPE_ERROR
                else -> UNKNOWN_ERROR
            }

            //TODO: compare with Ajv error on JS Schema
            outErrors.add(org.dashevo.schema.model.Error(
                    error.errorMessage,
                    errorType.toString(),
                    error.toJSON().toString(),
                    error.pointerToViolation,
                    error.violatedSchema.schemaLocation,
                    error.violatedSchema.title
            ))
        }

        return outErrors
    }

}