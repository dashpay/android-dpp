package org.dashevo.schema

import org.assertj.core.api.Assertions.assertThat
import org.dashevo.schema.model.Rules
import org.json.JSONObject
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

@DisplayName("DAP Objects")
class DapObjectTest {

    private val dapobjects = JSONObject(File("src/test/resources/data/dapobjects.json").readText())
    private val dapSchema = JSONObject(File("src/test/resources/data/somedap.json").readText())

    @Nested
    @DisplayName("Valid Dap Objects")
    inner class ValidDapObjects {

        @Test
        @DisplayName("valid dapobject 1")
        fun validDapObject1() {
            val validDapObject = dapobjects.getJSONObject("valid_dapobject_1")
            val valid = Validate.validateDapObject(validDapObject, dapSchema)
            assertThat(valid.valid).isTrue()
        }

        @Test
        @DisplayName("valid dapobject 2")
        fun validDapObject2() {
            val validDapObject = dapobjects.getJSONObject("valid_dapobject_2")
            val valid = Validate.validateDapObject(validDapObject, dapSchema)
            assertThat(valid.valid).isTrue()
        }

    }

    @Nested
    @DisplayName("DapObject Properties")
    inner class Properties {

        @Test
        @DisplayName("allow additional properties")
        fun additionalProperties() {
            val additionalPropertyDapObject = dapobjects.getJSONObject("additional_properties")
            val valid = Validate.validateDapObject(additionalPropertyDapObject, dapSchema)
            assertThat(valid.valid).isTrue()
        }

        @Test
        @DisplayName("missing inherited properties")
        fun missingInheritedProperties() {
            val missingPropertiesDapObject = dapobjects.getJSONObject("missing_inherited_properties")
            val valid = Validate.validateDapObject(missingPropertiesDapObject, dapSchema)
            assertThat(valid.valid).isFalse()
        }

        @Test
        @DisplayName("missing local properties")
        fun missingLocalProperties() {
            val missingLocalPropertiesDapObject = dapobjects.getJSONObject("missing_local_properties")
            val valid = Validate.validateDapObject(missingLocalPropertiesDapObject, dapSchema)
            assertThat(valid.valid).isFalse()
        }

    }

    @Nested
    @DisplayName("DapObject Type")
    inner class DapObjectType {

        @Test
        @DisplayName("missing object type")
        fun missingObjectType() {
            val missingObjectTypeDapObject = dapobjects.getJSONObject("missing_object_type")
            val valid = Validate.validateDapObject(missingObjectTypeDapObject, dapSchema)

            assertThat(valid.valid).isFalse()
            assertThat(valid.errCode).isEqualTo(Rules.DAPOBJECT_MISSING_OBJTYPE.code)
        }

        @Test
        @DisplayName("unknown object type")
        fun unknownObjectType() {
            val unknownObjectTypDapObject = dapobjects.getJSONObject("unknown_object_type")
            val valid = Validate.validateDapObject(unknownObjectTypDapObject, dapSchema)

            assertThat(valid.valid).isFalse()
            assertThat(valid.errCode).isEqualTo(Rules.DAPOBJECT_UNKNOWN_OBJTYPE.code)
        }

        @Test
        @DisplayName("mismatched object type")
        fun mismatchedObjectType() {
            val mismatchedObjectTypeDapObject = dapobjects.getJSONObject("mismatched_object_type")
            val valid = Validate.validateDapObject(mismatchedObjectTypeDapObject, dapSchema)

            assertThat(valid.valid).isFalse()
        }

    }

}