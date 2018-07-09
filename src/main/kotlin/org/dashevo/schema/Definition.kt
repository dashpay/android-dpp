package org.dashevo.schema

import org.json.JSONArray
import org.json.JSONObject

object Definition {

    private const val RELATION_DEFINITION = "http://dash.org/schemas/sys#/definitions/relation"

    fun getDapSubSchema(obj: JSONObject, dapSchema: JSONObject): JSONObject? {
        val key = obj.optString("objtype")
        return if (dapSchema.has(key)) {
            dapSchema.getJSONObject(key)
        } else {
            null
        }
    }

    /**
     * Get schema relations
     *
     * @param {object} dapSchema
     * @return {object}
     */
    fun getSchemaRelations(dapSchema: JSONObject): JSONObject {
        val subSchemasKeys = dapSchema.keys()

        val relations = JSONObject()
        subSchemasKeys.forEach { subSchemaKey ->
            val subSchemaRelations = getSubSchemaRelations(dapSchema, subSchemaKey)
            if (subSchemaRelations != null && subSchemaRelations.length() > 0) {
                relations.put(subSchemaKey, subSchemaRelations)
            }
        }
        return relations
    }

    /**
     * Get sub schema relations
     *
     * @param {object} dapSchema
     * @param {string} subSchemaKey
     * @return {string[]}
     */
    fun getSubSchemaRelations(dapSchema: JSONObject, subSchemaKey: String?): JSONArray? {
        val subSchema = dapSchema.getJSONObject(subSchemaKey)
        val subSchemaProperties = subSchema.getJSONObject("properties") ?: return null

        val relations = JSONArray()
        subSchemaProperties.keys().forEach{
            val propertyDefinition = subSchemaProperties.getJSONObject(it)
            if (propertyDefinition.has("\$ref") && propertyDefinition.getString("\$ref") == RELATION_DEFINITION) {
                relations.put(it)
            }
        }

        return relations
    }

}