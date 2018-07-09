package org.dashevo.schema

import org.dashevo.schema.Object.ALL_OF
import org.dashevo.schema.Object.PROPERTIES
import org.dashevo.schema.Object.REF
import org.dashevo.schema.Object.S_SCHEMA
import org.dashevo.schema.Object.TITLE
import org.dashevo.schema.model.Result
import org.dashevo.schema.model.Rules
import org.dashevo.schema.util.JsonSchemaUtils
import org.json.JSONArray
import org.json.JSONObject

object Compile {

    fun compileSysSchema(): Result {
        return JsonSchemaUtils.validateSysSchemaDef(Schema.system)
    }

    /**
     * Validate a DapSchema definition
     * @param dapSchema {object} DapSchema
     * @returns {*}
     */
    fun compileDapSchema(dapSchema: JSONObject): Result {
        // valid $schema tag
        if (!dapSchema.has(S_SCHEMA) || dapSchema.getString(S_SCHEMA) != Params.dapSchemaMetaURI) {
            return Result(Rules.INVALID_METASCHEMA.code, "DAPSchema", "\$schemaid")
        }

        //has title
        if (dapSchema.get(TITLE) !is String || dapSchema.getString(TITLE).length !in 3..24) {
            return Result(Rules.INVALID_SCHEMA_TITLE.code, "DAPSchema", "title")
        }

        //subschema count
        val count = dapSchema.keySet().size
        if (count !in 3..1002) {
            return Result(Rules.INVALID_DAP_SUBSCHEMA_COUNT.code, "DAP Subschema", "count")
        }

        //check subschemas
        subSchemas@ for (i in 0..count) {
            val keyword = dapSchema.keySet().elementAt(i)

            if (keyword == S_SCHEMA || keyword == TITLE) {
                continue@subSchemas
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

            val subSchema = dapSchema.getJSONObject(keyword)
            //schema inheritance
            if (!subSchema.has(ALL_OF)) {
                return Result(Rules.DAP_SUBSCHEMA_INHERITANCE.code, "dap subschema inheritance missing", keyword)
            }

            if (subSchema.get(ALL_OF) !is JSONArray) {
                return Result(Rules.DAP_SUBSCHEMA_INHERITANCE.code, "dap subschema inheritance invalid", keyword)
            } else if (subSchema.getJSONArray(ALL_OF).getJSONObject(0).getString(REF) !=
                    Params.dapObjectBaseRef) {
                return Result(Rules.DAP_SUBSCHEMA_INHERITANCE.code, "dap subschema inheritance invalid", keyword)
            }

        }

        return JsonSchemaUtils.validateDapSchemaDef(dapSchema)
    }

}