package org.dashevo.schema

import org.jsonorg.JSONObject

interface SchemaLoader {

    fun loadJsonSchema(): JSONObject
    fun loadDashSystemSchema(): JSONObject

}