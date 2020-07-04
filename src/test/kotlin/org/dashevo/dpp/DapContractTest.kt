package org.dashevo.dpp

import org.json.JSONObject
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

@DisplayName("DAP Contract Tests")
class DapContractTest : BaseTest() {

    private val dapContract = JSONObject(File("src/test/resources/data/datacontract.json").readText())

    @Nested
    @DisplayName("DapContract Schema")
    inner class DapContractSchema {

        @Test
        @DisplayName("Validate DapContract Container")
        fun validateDapContractContainer() {
        }

    }

}