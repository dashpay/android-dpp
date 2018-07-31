package org.dashevo.schema

import org.json.JSONObject
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.skyscreamer.jsonassert.JSONAssert
import java.io.File

@DisplayName("Serialize Tests")
class SerializeTest {

    val json: JSONObject = JSONObject(File("src/test/resources/data/valid_subtx.json").readText())

    @Test
    @DisplayName("Encode JSON Test")
    fun encodeJsonTest() {
        val byteArray = Serialize.encode(json)
        val decodedJson = Serialize.decode(byteArray)
        JSONAssert.assertEquals(decodedJson, json, false)
    }

}