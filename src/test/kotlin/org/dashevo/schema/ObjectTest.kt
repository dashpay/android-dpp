package org.dashevo.schema

import org.assertj.core.api.Assertions.assertThat
import org.dashevo.schema.Object.ACT
import org.dashevo.schema.Object.REMOVE_OBJECT_ACTION
import org.dashevo.schema.Object.REV
import org.jsonorg.JSONObject
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Schema.Object unit tests")
class ObjectTest {

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

    @Nested
    @DisplayName("Object manipulation")
    inner class ObjectManipulation {

        @Test
        @DisplayName("prepare for removal")
        fun prepareForRemoval() {
            val dapObject = JSONObject(mapOf(
                    REV to 0
            ))
            Object.prepareForRemoval(dapObject)
            assertThat(dapObject.getInt(ACT)).isEqualTo(REMOVE_OBJECT_ACTION)
            assertThat(dapObject.getInt(REV)).isEqualTo(1)
        }

    }

}