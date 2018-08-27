package org.dashevo.schema

import org.assertj.core.api.Assertions.assertThat
import org.dashevo.schema.model.Result
import org.dashevo.schema.util.JsonSchemaUtils
import org.everit.json.schema.ValidationException
import org.jsonorg.JSONObject
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

@DisplayName("Packet Tests")
class STPacketTest {

    private val dapSchema = JSONObject(File("src/test/resources/data/somedap.json").readText())
    private val testData = JSONObject(File("src/test/resources/data/stpacket-test-data.json").readText())
    private val data = JSONObject()

    fun validateAgainstSystemSchema(obj: JSONObject): Result {
        var valid = Result()
        try {
            Validate.createValidator(Schema.system).validate(obj)
        } catch (e: ValidationException) {
            valid = JsonSchemaUtils.convertValidationError(e.causingExceptions, "")
        }
        return valid
    }

    @Nested
    @DisplayName("DapContract Packet")
    inner class DapContractPacket {

        @Test
        @DisplayName("valid dapcontract object")
        fun validDapContractObject() {
            val obj = testData.getJSONObject("dapcontract_object")
            obj.getJSONObject("dapcontract").put("dapschema", dapSchema)

            val valid = Validate.validateDapContract(obj)
            assertThat(valid.valid).isTrue()
        }

    }

    @Nested
    @DisplayName("DapSpace Packet")
    inner class DapSpacePacket {

        @Test
        @DisplayName("valid packet")
        fun validPacket() {
            val obj = testData.getJSONObject("dapspace_valid_packet")
            data.put("validPacket", obj)

            val valid =  Validate.validateSTPacket(obj, dapSchema)
            assertThat(valid.valid).isTrue()
        }

        @Test
        @DisplayName("missing list")
        fun missingList() {
            val obj = testData.getJSONObject("dapspace_missing_list")
            data.put("invalidPacket", obj)

            val valid = Validate.validateSTPacket(obj, dapSchema)
            assertThat(valid.valid).isFalse()
        }

    }

    @Nested
    @DisplayName("Packet Creation")
    inner class PacketCreation {

        @Test
        @DisplayName("filter additional fields from dapcontract-contract packet")
        fun filterAdditionalFields() {
            val obj = testData.getJSONObject("packet_creation_filter_additional_fields")
            val schemaObj = Object.fromObject(obj)
            val dapContract = Object.fromObject(obj.getJSONObject("stpacket").getJSONObject("dapcontract"))
            schemaObj.getJSONObject("stpacket").put("dapcontract", dapContract)

            assertThat(schemaObj.getJSONObject("stpacket").has("unknown1")).isFalse()
            assertThat(schemaObj.getJSONObject("stpacket").getJSONObject("dapcontract").has("unknown2")).isFalse()
        }

    }

    @Nested
    @DisplayName("Packet Instance")
    inner class PacketInstance {

        @Test
        @DisplayName("valid dapcontract-contract packet")
        fun validDapContract() {
            val obj = testData.getJSONObject("packet_instance_valid_dapcontract")
            assertThat(validateAgainstSystemSchema(obj).valid).isTrue()
        }

        @Test
        @DisplayName("invalid dapobjects packet with null dapobjects")
        fun invalidDapObjectsPacket() {
            val obj = testData.getJSONObject("packet_instance_invalid_dapobjects")
            data.put("createPacket", obj)

            val valid = Validate.validateSTPacket(obj)
            assertThat(valid.valid).isFalse()
        }

        @Test
        @DisplayName("invalidate multiple packet-content subschemas")
        fun invalidMultiplePacket() {
            val obj = testData.getJSONObject("packet_instance_invalid_multiple_packet")
            data.put("invalid_packet_multi_subschema", obj)

            val valid = Validate.validateSTPacket(obj)
            assertThat(valid.valid).isFalse()
        }

        @Test
        @DisplayName("invalid empty packet")
        fun invalidEmptyPacket() {
            val obj = testData.getJSONObject("packet_instance_invalid_empty_packet")
            val valid = Validate.validateSTPacket(obj)
            assertThat(valid.valid).isFalse()
        }

    }

}