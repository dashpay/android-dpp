package org.dashevo.schema

import org.assertj.core.api.Assertions.assertThat
import org.dashevo.schema.util.JsonSchemaUtils
import org.json.JSONObject
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

/**
 *  Here we test JSON schema draft compatibility with Dash schema patterns
 *  using a simplified inline Dash System schema and later with a single extended DAP schema
 *
 *  Current JSON schema spec is draft #7:
 *  http://json-schema.org/draft-07/schema#
 *
 *  NOTES:
 *
 *  - additionalProperties keyword is used for System and Dap Schema root properties but not for subschemas
 *    this means objects can have additional properties and still validate, therefore the pattern is to ignore
 *    additional properties not specified in the schema in consensus code
 *
 *  - ...we use $ref and definitions section for schema inheritance
 */
@DisplayName("JSON Schema Draft ")
class JsonSchemaTest {

    init {
        //Simplified System Schema
        Schema.system = JSONObject(File("src/test/resources/data/simplified-system-schema.json").readText())
    }

    val simplifiedDapSchema =  JSONObject(File("src/test/resources/data/simplified-dap-schema.json").readText())
    val data =  JSONObject(File("src/test/resources/data/jsonschema-test-data.json").readText())

    @Nested
    @DisplayName("System Schema")
    inner class SystemSchema {

        @Test
        @DisplayName("valid inherited sys object")
        fun validInheritedSysObject() {
            val obj = data.getJSONObject("valid_inherited_sys_object")
            val valid = JsonSchemaUtils.validateSchemaObject(obj)
            assertThat(valid.valid).isTrue()
        }

        @Test
        @DisplayName("missing required field")
        fun missingRequiredField() {
            val obj = data.getJSONObject("missing_required_field")
            val valid = JsonSchemaUtils.validateSchemaObject(obj)
            assertThat(valid.valid).isFalse()
        }

    }

}