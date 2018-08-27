package org.dashevo.schema

import org.assertj.core.api.Assertions.assertThat
import org.jsonorg.JSONObject
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.io.File

@DisplayName("SubTx Tests")
class SubTxTest : BaseTest() {

    @Nested
    @DisplayName("SubTx Hash")
    inner class SubTxHash {

        @Test
        @DisplayName("validate a SubTx Hash")
        fun validateSubTxHash() {
            val aliceSubTx = data.getJSONObject("alice_subtx_1")
            val aliceId = aliceSubTx.getJSONObject("subtx").getJSONObject("meta").getString("id")
            assertThat(Hash.subtx(aliceSubTx)).isEqualTo(aliceId)
        }

    }

    @Nested
    @DisplayName("SubTx Raw")
    inner class Raw {

        @Test
        @DisplayName("valid subtx")
        fun validSubTx() {
            val validSubTx = JSONObject(File("src/test/resources/data/valid_subtx.json").readText())
            assertThat(Validate.validateSubTx(validSubTx).valid).isTrue()
        }

    }

}