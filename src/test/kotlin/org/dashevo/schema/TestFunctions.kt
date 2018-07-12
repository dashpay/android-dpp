package org.dashevo.schema

import org.dashevo.schema.model.Result
import org.dashevo.schema.util.JsonSchemaUtils
import org.everit.json.schema.ValidationException
import org.json.JSONObject

fun validateAgainstSystemSchema(obj: JSONObject): Result {
    var valid = Result()
    try {
        Validate.createValidator(Schema.system).validate(obj)
    } catch (e: ValidationException) {
        valid = JsonSchemaUtils.convertValidationError(e.causingExceptions, "")
    }
    return valid
}
