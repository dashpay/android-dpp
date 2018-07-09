package org.dashevo.schema

import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

@DisplayName("DAP Contract Tests")
class DapContractTest : BaseTest() {

    private val dapContract = JSONObject(File("src/test/resources/data/dapcontract.json").readText())

    @Nested
    @DisplayName("DapContract Schema")
    inner class DapContractSchema {

        @Test
        @DisplayName("Valid DapContract Schema")
        fun validDapContractSchema() {
            val valid = Compile.compileDapSchema(contactsDap)
            assertThat(valid.valid).isTrue()
        }

        @Test
        @DisplayName("Validate DapContract Container")
        fun validateDapContractContainer() {
            dapContract.getJSONObject("dapcontract").put("dapschema", contactsDap)

            val valid = Validate.validateDapContract(dapContract)
            assertThat(valid.valid).isTrue()
        }

    }

}