package org.dashevo.schema

import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

@DisplayName("Schema.Object unit tests")
class ObjectTest {

    init {
        Schema.system = JSONObject(File("src/test/resources/data/dash-system-schema.json").readText())
    }

    fun dapObj(): JSONObject {
        return JSONObject(hashMapOf(
                "someprop" to 1
        ))
    }

    fun sysObj(): JSONObject {
        return JSONObject(hashMapOf(
                "subtx" to dapObj()
        ))
    }

    @Nested
    @DisplayName("Meta data")
    inner class MetaData {

        private val metaObj = dapObj()

        @Test
        @DisplayName("create metadata")
        fun createMetaData() {
            Object.setMeta(metaObj, "somekey", 1)
            assertThat(metaObj.getJSONObject("meta").getInt("somekey")).isEqualTo(1)
        }

        @Test
        @DisplayName("append metadata")
        fun appendMetaData() {
            Object.setMeta(metaObj, "somekey2", 2)
            assertThat(metaObj.getJSONObject("meta").getInt("somekey2")).isEqualTo(2)
        }

    }

    @Nested
    @DisplayName("Object classification")
    inner class ObjectClassification {

        @Test
        @DisplayName("system object")
        fun systemObject() {
            assertThat(Object.isSysObject(sysObj())).isTrue()
        }

        @Test
        @DisplayName("dap object")
        fun dapObject() {
            assertThat(Object.isSysObject(dapObj())).isFalse()
        }

        @Test
        @DisplayName("empty object")
        fun emptyObject() {
            val obj = JSONObject()
            assertThat(Object.isSysObject(obj)).isFalse()
        }

        @Test
        @DisplayName("null object")
        fun nullObject() {
            assertThat(Object.isSysObject(null)).isFalse()
        }

    }

}