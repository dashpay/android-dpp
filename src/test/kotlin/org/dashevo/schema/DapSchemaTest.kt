package org.dashevo.schema

import org.assertj.core.api.Assertions.assertThat
import org.dashevo.schema.model.Rules
import org.json.JSONObject
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

@DisplayName("DapSchema Tests")
class DapSchemaTest {

    init {
        Schema.system = JSONObject(File("src/test/resources/data/dash-system-schema.json").readText())
    }

    @Nested
    @DisplayName("Invalid Dap Schemas")
    inner class InvalidSchemas {

        @Test
        @DisplayName("missing meta schema")
        fun missingMetaSchema() {
            val dapSchema = JSONObject()
            val valid = Compile.compileDapSchema(dapSchema)
            assertThat(valid.errCode).isEqualTo(Rules.INVALID_METASCHEMA.code)
        }

        @Test
        @DisplayName("missing schema title")
        fun missingSchemaTitle() {
            val dapSchema = JSONObject(hashMapOf(
                    "\$schema" to Params.dapSchemaMetaURI
            ))
            val valid = Compile.compileDapSchema(dapSchema)
            assertThat(valid.errCode).isEqualTo(Rules.INVALID_SCHEMA_TITLE.code)
        }

        @Test
        @DisplayName("schema title too short")
        fun shortSchemaTitle() {
            val dapSchema = JSONObject(hashMapOf(
                    "\$schema" to Params.dapSchemaMetaURI,
                    "title" to "ab"
            ))
            val valid = Compile.compileDapSchema(dapSchema)
            assertThat(valid.errCode).isEqualTo(Rules.INVALID_SCHEMA_TITLE.code)
        }

        @Test
        @DisplayName("schema title too long")
        fun longSchemaTitle() {
            val dapSchema = JSONObject(hashMapOf(
                    "\$schema" to Params.dapSchemaMetaURI,
                    "title" to "abcdefghijklmnopqrstuvwxy"
            ))
            val valid = Compile.compileDapSchema(dapSchema)
            assertThat(valid.errCode).isEqualTo(Rules.INVALID_SCHEMA_TITLE.code)
        }

    }

    @Nested
    @DisplayName("Invalid DAP subschema names")
    inner class InvalidSubschemaNames {

        @Test
        @DisplayName("no subschemas")
        fun noSubSchemas() {
            val dapSchema = JSONObject(hashMapOf(
                    "\$schema" to Params.dapSchemaMetaURI,
                    "title" to "abcdef"
            ))
            val valid = Compile.compileDapSchema(dapSchema)
            assertThat(valid.errCode).isEqualTo(Rules.INVALID_DAP_SUBSCHEMA_COUNT.code)
        }

        @Test
        @DisplayName("more than max dap subschemas")
        fun exceedSubSchemasCount() {
            val dapSchema = JSONObject(hashMapOf(
                    "\$schema" to Params.dapSchemaMetaURI,
                    "title" to "abcdef"
            ))

            for (i in 0 until 1001) {
                dapSchema.put("subschema$i", JSONObject())
            }

            val valid = Compile.compileDapSchema(dapSchema)
            assertThat(valid.errCode).isEqualTo(Rules.INVALID_DAP_SUBSCHEMA_COUNT.code)
        }

        @Test
        @DisplayName("invalid dap subchema name (reserved params keyword)")
        fun reservedParamsKeyword() {
            val dapSchema = JSONObject(hashMapOf(
                    "\$schema" to Params.dapSchemaMetaURI,
                    "title" to "abcdef",
                    "type" to "1"
            ))

            val valid = Compile.compileDapSchema(dapSchema)
            assertThat(valid.errCode).isEqualTo(Rules.RESERVED_DAP_SUBSCHEMA_NAME.code)
        }

        @Test
        @DisplayName("invalid dap subchema name (reserved sysobject keyword)")
        fun reservedSysObjectKeyword() {
            val dapSchema = JSONObject(hashMapOf(
                    "\$schema" to Params.dapSchemaMetaURI,
                    "title" to "abcdef",
                    "subtx" to "1"
            ))

            val valid = Compile.compileDapSchema(dapSchema)
            assertThat(valid.errCode).isEqualTo(Rules.RESERVED_DAP_SUBSCHEMA_NAME.code)
        }

        @Test
        @DisplayName("invalid dap subchema name (reserved syschema definition keyword)")
        fun reservedSysSchemaKeyword() {
            val dapSchema = JSONObject(hashMapOf(
                    "\$schema" to Params.dapSchemaMetaURI,
                    "title" to "abcdef",
                    "dapobjectbase" to "1"
            ))

            val valid = Compile.compileDapSchema(dapSchema)
            assertThat(valid.errCode).isEqualTo(Rules.RESERVED_DAP_SUBSCHEMA_NAME.code)
        }

        @Test
        @DisplayName("invalid dap subchema name (disallowed characters)")
        fun disallowedCharacters() {
            val dapSchema = JSONObject(hashMapOf(
                    "\$schema" to Params.dapSchemaMetaURI,
                    "title" to "abcdef",
                    "#" to "1"
            ))

            val valid = Compile.compileDapSchema(dapSchema)
            assertThat(valid.errCode).isEqualTo(Rules.INVALID_DAP_SUBSCHEMA_NAME.code)
        }

        @Test
        @DisplayName("invalid dap subchema name (below min length)")
        fun nameBelowMinLength() {
            val dapSchema = JSONObject(hashMapOf(
                    "\$schema" to Params.dapSchemaMetaURI,
                    "title" to "abcdef",
                    "ab" to "1"
            ))

            val valid = Compile.compileDapSchema(dapSchema)
            assertThat(valid.errCode).isEqualTo(Rules.INVALID_DAP_SUBSCHEMA_NAME.code)
        }

        @Test
        @DisplayName("invalid dap subchema name (above max length)")
        fun nameAboveMaxLength() {
            val dapSchema = JSONObject(hashMapOf(
                    "\$schema" to Params.dapSchemaMetaURI,
                    "title" to "abcdef",
                    "abcdefghijklmnopqrstuvwxy" to "1"
            ))

            val valid = Compile.compileDapSchema(dapSchema)
            assertThat(valid.errCode).isEqualTo(Rules.INVALID_DAP_SUBSCHEMA_NAME.code)
        }

    }

    @Nested
    @DisplayName("Invalid DAP subschema contents")
    inner class InvalidSubSchemaContents {

        @Test
        @DisplayName("missing DAP subschema inheritance")
        fun missingSubSchemaInheritance() {
            val dapSchema = JSONObject(hashMapOf(
                    "\$schema" to Params.dapSchemaMetaURI,
                    "title" to "abcdef",
                    "someobject" to "1"
            ))

            val valid = Compile.compileDapSchema(dapSchema)
            assertThat(valid.errCode).isEqualTo(Rules.DAP_SUBSCHEMA_INHERITANCE.code)
        }

        @Test
        @DisplayName("invalid DAP subschema inheritance (missing allOf)")
        fun missingAllOf() {
            val dapSchema = JSONObject(hashMapOf(
                    "\$schema" to Params.dapSchemaMetaURI,
                    "title" to "abcdef",
                    "someobject" to JSONObject()
            ))

            val valid = Compile.compileDapSchema(dapSchema)
            assertThat(valid.errCode).isEqualTo(Rules.DAP_SUBSCHEMA_INHERITANCE.code)
        }

        @Test
        @DisplayName("invalid DAP subschema inheritance (invalid type)")
        fun invalidType() {
            val dapSchema = JSONObject(hashMapOf(
                    "\$schema" to Params.dapSchemaMetaURI,
                    "title" to "abcdef",
                    "someobject" to JSONObject(hashMapOf(
                            "allOf" to JSONObject(hashMapOf(
                                    "\$ref" to Params.dapObjectBaseRef
                            ))
                    ))
            ))

            val valid = Compile.compileDapSchema(dapSchema)
            assertThat(valid.errCode).isEqualTo(Rules.DAP_SUBSCHEMA_INHERITANCE.code)
        }

        @Test
        @DisplayName("invalid DAP subschema inheritance (missing \$ref)")
        fun missingRef() {
            val dapSchema = JSONObject(hashMapOf(
                    "\$schema" to Params.dapSchemaMetaURI,
                    "title" to "abcdef",
                    "someobject" to JSONObject(hashMapOf(
                            "allOf" to listOf(JSONObject())
                    ))
            ))

            val valid = Compile.compileDapSchema(dapSchema)
            assertThat(valid.errCode).isEqualTo(Rules.DAP_SUBSCHEMA_INHERITANCE.code)
        }

        @Test
        @DisplayName("invalid DAP subschema inheritance (unknown \$ref)")
        fun unknownRef() {
            val dapSchema = JSONObject(hashMapOf(
                    "\$schema" to Params.dapSchemaMetaURI,
                    "title" to "abcdef",
                    "someobject" to JSONObject(hashMapOf(
                            "allOf" to listOf(JSONObject(hashMapOf(
                                    "\$ref" to "unknown"
                            )))
                    ))
            ))

            val valid = Compile.compileDapSchema(dapSchema)
            assertThat(valid.errCode).isEqualTo(Rules.DAP_SUBSCHEMA_INHERITANCE.code)
        }

    }

}