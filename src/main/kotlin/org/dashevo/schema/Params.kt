package org.dashevo.schema

object Params {
    val dapSchemaIdURI = "http://dash.org/schemas/dapschema"
    val sysSchemaMetaURI = "http://json-schema.org/draft-07/schema#"
    val sysSchemaId = "http://dash.org/schemas/sys"
    val dapMetaSchema = "http://dash.org/schemas/sys"
    val reservedKeywords = arrayOf("dash")
    val dapObjectBaseRef = "http://dash.org/schemas/sys#/definitions/dapobjectbase"
    val dapSchemaMaxSize = 10000
    val dashSchemaKeywords = arrayOf("_key", "_isrole", "_owningroles", "_objtype", "_relation", "_multiplicity", "_scope")
}