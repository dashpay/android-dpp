package org.dashevo.schema

import org.jsonorg.JSONObject
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.io.File

@DisplayName("Serialize Tests")
class SerializeTest {

    val json: JSONObject = JSONObject(File("src/test/resources/data/valid_subtx.json").readText())

    @Test
    @DisplayName("Encode JSON Test")
    fun encodeJsonTest() {
        //TODO
    }

}