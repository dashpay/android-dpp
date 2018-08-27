package org.dashevo.schema

import org.jsonorg.JSONObject
import java.io.File

object Schema {

    var schemaLoader: SchemaLoader = object : SchemaLoader {
        override fun loadJsonSchema(): JSONObject {
            return JSONObject(File(Schema::class.java.getResource("/schema_v7.json").path).readText())
        }

        override fun loadDashSystemSchema(): JSONObject {
            return JSONObject(File(Schema::class.java.getResource("/dash_system_schema.json").path).readText())
        }
    }

    val system by lazy {
        schemaLoader.loadDashSystemSchema()
    }

    val jsonSchema by lazy {
        schemaLoader.loadJsonSchema()
    }

}
