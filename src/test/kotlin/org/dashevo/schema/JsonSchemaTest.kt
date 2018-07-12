package org.dashevo.schema

import org.assertj.core.api.Assertions.assertThat
import org.dashevo.schema.model.Result
import org.dashevo.schema.util.JsonSchemaUtils
import org.everit.json.schema.ValidationException
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

    val dapSchema = JSONObject(File("src/test/resources/data/simplified-dap-schema.json").readText())
    val data = JSONObject(File("src/test/resources/data/jsonschema-test-data.json").readText())

    fun validateAgainstDapSchema(obj: JSONObject, dapObjectIndex: Int = 0): Result {
        var valid = Result()
        try {
            Validate.createValidator(dapSchema).validate(obj
                    .getJSONObject("dapobjectcontainer").getJSONArray("dapobjects")[dapObjectIndex])
        } catch (e: ValidationException) {
            valid = JsonSchemaUtils.convertValidationError(e.causingExceptions, "")
        }
        return valid
    }

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

        @Test
        @DisplayName("missing required field in super")
        fun missingRequiredFieldInSuper() {
            val obj = data.getJSONObject("missing_required_field_in_super")
            val valid = JsonSchemaUtils.validateSchemaObject(obj)
            assertThat(valid.valid).isFalse()
        }

        @Test
        @DisplayName("missing required field in base")
        fun missingRequiredFieldInBase() {
            val obj = data.getJSONObject("missing_required_field_in_base")
            val valid = JsonSchemaUtils.validateSchemaObject(obj)
            assertThat(valid.valid).isFalse()
        }

        @Test
        @DisplayName("no valid schema")
        fun noValidSchema() {
            val obj = data.getJSONObject("no_valid_schema")
            val valid = JsonSchemaUtils.validateSchemaObject(obj)
            assertThat(valid.valid).isFalse()
        }

        @Test
        @DisplayName("prevent additional properties in main sys schema")
        fun additionalPropertiesInMainSchema() {
            val obj = data.getJSONObject("additional_properties_in_main_schema")
            val valid = JsonSchemaUtils.validateSchemaObject(obj)
            assertThat(valid.valid).isFalse()
        }

        @Test
        @DisplayName("allow additional properties in sys subschemas")
        fun additionalPropertiesInSysSubSchema() {
            val obj = data.getJSONObject("additional_properties_is_sys_subschema")
            val valid = JsonSchemaUtils.validateSchemaObject(obj)
            assertThat(valid.valid).isTrue()
        }

    }

    @Nested
    @DisplayName("System Schema Containers")
    inner class SystemSchemaContainers {

        @Test
        @DisplayName("valid container")
        fun validContainer() {
            val obj = data.getJSONObject("valid_container")
            val valid = JsonSchemaUtils.validateSchemaObject(obj)
            assertThat(valid.valid).isTrue()
        }

        @Test
        @DisplayName("missing list")
        fun missingList() {
            val obj = data.getJSONObject("missing_list")
            val valid = JsonSchemaUtils.validateSchemaObject(obj)
            assertThat(valid.valid).isFalse()
        }

        @Test
        @DisplayName("null list")
        fun nullList() {
            val obj = data.getJSONObject("null_list")
            val valid = JsonSchemaUtils.validateSchemaObject(obj)
            assertThat(valid.valid).isFalse()
        }

        @Test
        @DisplayName("empty list")
        fun emptyList() {
            val obj = data.getJSONObject("empty_list")
            val valid = JsonSchemaUtils.validateSchemaObject(obj)
            assertThat(valid.valid).isFalse()
        }

        @Test
        @DisplayName("incorrect item type")
        fun incorrectItemType() {
            val obj = data.getJSONObject("incorrect_item_type")
            val valid = JsonSchemaUtils.validateSchemaObject(obj)
            assertThat(valid.valid).isFalse()
        }

        @Test
        @DisplayName("missing array item required field")
        fun missingArrayItemRequired() {
            val obj = data.getJSONObject("missing_array_item_required_field")
            val valid = JsonSchemaUtils.validateSchemaObject(obj)
            assertThat(valid.valid).isFalse()
        }

        @Test
        @DisplayName("missing array item required base field")
        fun missingArrayItemRequiredBaseField() {
            val obj = data.getJSONObject("missing_array_item_required_base_field")
            val valid = JsonSchemaUtils.validateSchemaObject(obj)
            assertThat(valid.valid).isFalse()
        }

        @Test
        @DisplayName("prevent multiple subschema-type definitions")
        fun preventMultipleSubSchemaTypeDefinitions() {
            val obj = data.getJSONObject("prevent_multiple_subschematype_definitions")
            val valid = JsonSchemaUtils.validateSchemaObject(obj)
            assertThat(valid.valid).isFalse()
        }


        @Test
        @DisplayName("prevent additional item types")
        fun preventAdditionalItemTypes() {
            //TODO: https://github.com/dashevo/dash-schema/issues/32
            val obj = data.getJSONObject("prevent_additional_item_types")
            val valid = JsonSchemaUtils.validateSchemaObject(obj)
            assertThat(valid.valid).isFalse()
        }

        @Test
        @DisplayName("prevent duplicate items")
        fun preventDuplicateItems() {
            val obj = data.getJSONObject("prevent_duplicate_items")
            val valid = JsonSchemaUtils.validateSchemaObject(obj)
            assertThat(valid.valid).isFalse()
        }

    }

    @Nested
    @DisplayName("DapContract Schema")
    inner class DapContractSchema {

        @Test
        @DisplayName("valid dapcontract object")
        fun validDapContractObject() {
            val obj = data.getJSONObject("valid_dapcontract_object")
            val valid = JsonSchemaUtils.validateSchemaObject(obj, dapSchema)
            assertThat(valid.valid).isTrue()
        }

        @Test
        @DisplayName("missing required field")
        fun missingRequiredField() {
            val obj = data.getJSONObject("dapobject_missing_required_field")
            val valid = JsonSchemaUtils.validateSchemaObject(obj, dapSchema)
            assertThat(valid.valid).isFalse()
        }

        @Test
        @DisplayName("missing required field in super")
        fun missingRequiredFieldInSuper() {
            val obj = data.getJSONObject("dapobject_missing_required_field_in_super1")
            val valid = JsonSchemaUtils.validateSchemaObject(obj, dapSchema)
            assertThat(valid.valid).isFalse()
        }

        @Test
        @DisplayName("missing required field in base")
        fun missingRequiredFieldInBase() {
            val obj = data.getJSONObject("dapobject_missing_required_field_in_base")
            val valid = JsonSchemaUtils.validateSchemaObject(obj, dapSchema)
            assertThat(valid.valid).isFalse()
        }

        @Test
        @DisplayName("prevent additional properties in main dapcontract schema")
        fun preventAdditionalPropertiesInMainDapContractSchema() {
            val obj = data.getJSONObject("prevent_additional_properties_in_main_dapcontract_schema")
            val valid = JsonSchemaUtils.validateSchemaObject(obj, dapSchema)
            assertThat(valid.valid).isFalse()
        }

        @Test
        @DisplayName("allow additional properties in dapcontract subschemas")
        fun allowAdditionalPropertiesInDapContractSubSchemas() {
            val obj = data.getJSONObject("allow_additional_properties_in_dapcontract_subschemas")
            val valid = JsonSchemaUtils.validateSchemaObject(obj, dapSchema)
            assertThat(valid.valid).isTrue()
        }

    }

    @Nested
    @DisplayName("DapContract Object Container")
    inner class DapContractObjectContainer {

        @Test
        @DisplayName("valid container")
        fun validContainer() {
            val obj = data.getJSONObject("dapcontract_object_container")

            val valid0 = validateAgainstSystemSchema(obj)
            val valid1 = validateAgainstDapSchema(obj)

            assertThat(valid0.valid).isTrue()
            assertThat(valid1.valid).isTrue()
        }

        @Test
        @DisplayName("missing list")
        fun missingList() {
            val obj = data.getJSONObject("dapcontract_missing_list")

            var valid = Result()
            try {
                Validate.createValidator(Schema.system).validate(obj)
            } catch (e: ValidationException) {
                valid = JsonSchemaUtils.convertValidationError(e.causingExceptions, "")
            }

            assertThat(valid.valid).isFalse()
        }

        @Test
        @DisplayName("empty list")
        fun emptyList() {
            val obj = data.getJSONObject("dapcontract_missing_list")

            var valid = Result()
            try {
                Validate.createValidator(Schema.system).validate(obj)
            } catch (e: ValidationException) {
                valid = JsonSchemaUtils.convertValidationError(e.causingExceptions, "")
            }

            assertThat(valid.valid).isFalse()
        }

        @Test
        @DisplayName("incorrect item type")
        fun incorrectItemType() {
            val obj = data.getJSONObject("dapcontract_incorrect_item_type")

            val valid0 = validateAgainstSystemSchema(obj)
            val valid1 = validateAgainstDapSchema(obj)

            assertThat(valid0.valid).isTrue()
            assertThat(valid1.valid).isFalse()
        }

        @Test
        @DisplayName("missing array item required field")
        fun missingArrayItemRequiredField() {
            val obj = data.getJSONObject("dapcontract_missing_array_item_required_field")

            val valid0 = validateAgainstSystemSchema(obj)
            val valid1 = validateAgainstDapSchema(obj)

            assertThat(valid0.valid).isTrue()
            assertThat(valid1.valid).isFalse()
        }

        @Test
        @DisplayName("missing array item required base field")
        fun missingArrayItemRequiredBaseField() {
            val obj = data.getJSONObject("dapcontract_missing_array_item_required_base_field")

            val valid0 = validateAgainstSystemSchema(obj)
            val valid1 = validateAgainstDapSchema(obj)

            assertThat(valid0.valid).isFalse()
            assertThat(valid1.valid).isFalse()
        }

        @Test
        @DisplayName("prevent multiple subschema-type definitions")
        fun preventMultipleSubSchemaTypeDefinitions() {
            val obj = data.getJSONObject("dapcontract_prevent_multiple_subschematype_definitions")

            val valid0 = validateAgainstSystemSchema(obj)
            val valid1 = validateAgainstDapSchema(obj)
            val valid2 = validateAgainstDapSchema(obj, 1)

            assertThat(valid0.valid).isTrue()
            assertThat(valid1.valid).isTrue()
            assertThat(valid2.valid).isFalse()
        }

        @Test
        @DisplayName("prevent additional item types")
        fun preventAdditionalItemTypes() {
            val obj = data.getJSONObject("dapcontract_prevent_additional_item_types")

            val valid0 = validateAgainstSystemSchema(obj)
            val valid1 = validateAgainstDapSchema(obj)

            assertThat(valid0.valid).isTrue()
            assertThat(valid1.valid).isFalse()
        }

        @Test
        @DisplayName("prevent duplicate items")
        fun preventDuplicateItems() {
            val obj = data.getJSONObject("dapcontract_prevent_duplicate_items")

            val valid0 = validateAgainstSystemSchema(obj)
            val valid1 = validateAgainstDapSchema(obj)
            val valid2 = validateAgainstDapSchema(obj, 1)

            assertThat(valid0.valid).isFalse()
            assertThat(valid1.valid).isTrue()
            assertThat(valid2.valid).isTrue()
        }
        
    }

    @Nested
    @DisplayName("Sysmod Container")
    inner class SysmodContainer {

        @Test
        @DisplayName("valid container")
        fun validContainer() {
            val obj = data.getJSONObject("sysmod_container_valid")

            val valid = validateAgainstSystemSchema(obj)
            assertThat(valid.valid).isTrue()
        }

    }

}