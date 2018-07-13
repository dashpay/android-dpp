package org.dashevo.schema

import org.json.JSONObject
import java.io.File

object Schema {

    val system by lazy {
        JSONObject(File(Schema::class.java.getResource("/dash-system-schema.json").path).readText())
    }

}
