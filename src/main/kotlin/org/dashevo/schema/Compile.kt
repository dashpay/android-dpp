package org.dashevo.schema

import org.dashevo.schema.Object.ALL_OF
import org.dashevo.schema.Object.DEFINITIONS
import org.dashevo.schema.Object.PROPERTIES
import org.dashevo.schema.Object.REF
import org.dashevo.schema.Object.SCHEMA_ID
import org.dashevo.schema.Object.TITLE
import org.dashevo.schema.model.Result
import org.dashevo.schema.model.Rules
import org.dashevo.schema.util.JsonSchemaUtils
import org.jsonorg.JSONArray

import org.jsonorg.JSONObject

object Compile {

    fun compileSysSchema(): Result {
        return JsonSchemaUtils.validateSchemaDef(Schema.system)
    }

    /**
     * Validate a DapSchema definition
     * @param dapSchema {object} DapSchema
     * @returns {*}
     */
    fun compileDapSchema(dapSchema: JSONObject): Result {
        // valid $schema tag
        if (!dapSchema.has(SCHEMA_ID) || dapSchema.getString(SCHEMA_ID) != Params.dapSchemaIdURI) {
            return Result(Rules.INVALID_ID.code, "DAPSchema", "\$id")
        }

        //has title
        if (!dapSchema.has(TITLE) || dapSchema.get(TITLE) !is String || dapSchema.getString(TITLE).length !in 3..24) {
            return Result(Rules.INVALID_SCHEMA_TITLE.code, "DAPSchema", "title")
        }

        //subschema count
        val count = dapSchema.keySet().size
        if (count !in 3..1002) {
            return Result(Rules.INVALID_DAP_SUBSCHEMA_COUNT.code, "DAP Subschema", "count")
        }

        // validate the DAP Schema using JSON Schema
        var result = JsonSchemaUtils.validateDapSchemaDef(dapSchema)
        if (!result.valid) {
            return result
        }

        //check subschemas
        for (i in 0..count) {
            result = compileDAPSubschema(dapSchema, i)
            if (!result.valid) {
                return result
            }
        }

        return JsonSchemaUtils.validateDapSchemaDef(dapSchema)
    }

    private fun compileDAPSubschema(dapSchema: JSONObject, i: Int): Result {
        val keyword = dapSchema.keySet().elementAt(i)

        if (keyword == SCHEMA_ID || keyword == TITLE || keyword == SCHEMA_ID) {
            return Result()
        }

        if (keyword.length !in 3..24) {
            return Result(Rules.INVALID_DAP_SUBSCHEMA_NAME.code, "invalid name length", keyword)
        }

        // invalid chars
        val invalid = Regex("[^a-z0-9._]").containsMatchIn(keyword)
        if (invalid) {
            return Result(Rules.INVALID_DAP_SUBSCHEMA_NAME.code, "disallowed name characters", keyword)
        }

        //subschema reserved keyword from params
        Params.reservedKeywords.forEach {
            if (keyword == it) {
                return Result(Rules.RESERVED_DAP_SUBSCHEMA_NAME.code, "reserved param keyword", keyword)
            }
        }

        //subschema reserved keyword from sys schema properties
        Schema.system.getJSONObject(PROPERTIES).keys().forEach {
            if (keyword == it) {
                return Result(Rules.RESERVED_DAP_SUBSCHEMA_NAME.code, "reserved sysobject keyword", keyword)
            }
        }

        //subschema reserved keyword from sys schema definitions
        Schema.system.getJSONObject(DEFINITIONS).keys().forEach {
            if (keyword == it) {
                return Result(Rules.RESERVED_DAP_SUBSCHEMA_NAME.code, "reserved syschema definition keyword", keyword)
            }
        }

        val subSchema = dapSchema.optJSONObject(keyword) ?: JSONObject()
        //schema inheritance
        if (!subSchema.has(ALL_OF)) {
            return Result(Rules.DAP_SUBSCHEMA_INHERITANCE.code, "dap subschema inheritance missing", keyword)
        }

        if (subSchema.get(ALL_OF) !is JSONArray) {
            return Result(Rules.DAP_SUBSCHEMA_INHERITANCE.code, "dap subschema inheritance invalid", keyword)
        } else if (subSchema.getJSONArray(ALL_OF).getJSONObject(0).optString(REF, "") !=
                Params.dapObjectBaseRef) {
            return Result(Rules.DAP_SUBSCHEMA_INHERITANCE.code, "dap subschema inheritance invalid", keyword)
        }

        return JsonSchemaUtils.validateDapSubschemaDef(subSchema)
    }

}