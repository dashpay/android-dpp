package org.dashevo.schema.util

import org.dashevo.schema.model.Error
import org.dashevo.schema.model.ValidationResult

object ValidationResultUtils {

    /**
     * Create a Validation Result
     * @param validateErrors Array of validate errors
     * @returns {{valid: boolean, validateErrors: Array}}
     */
    fun result(validateErrors: List<Error>): ValidationResult {
        return ValidationResult(validateErrors.isEmpty(), validateErrors)
    }

    fun result(validateError: Error): ValidationResult {
        return result(listOf(validateError))
    }

}