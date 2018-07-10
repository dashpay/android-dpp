package org.dashevo.schema

import org.json.JSONObject
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.File

@DisplayName("SysSchema Tests")
class SysSchemaTest {

    init {
        Schema.system = JSONObject(File("src/test/resources/data/dash-system-schema.json").readText())
    }

    @Test
    @DisplayName("Valid Sys Schemas")
    fun validSysSchemas() {
    }

}