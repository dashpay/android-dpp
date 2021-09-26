/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashj.platform.dpp.util

import org.dashj.platform.dpp.ProtocolVersion
import org.dashj.platform.dpp.toHexString
import org.json.JSONArray
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CborTest {

    @Test
    fun array2cborTest() {
        val where: List<List<String>> = listOf(listOf("message", "startsWith", "Tutorial"))

        val array = JSONArray(where)
        assertEquals("[[\"message\",\"startsWith\",\"Tutorial\"]]", array.toString())

        val bytes = Cbor.encode(where)
        assertEquals("8183676d6573736167656a73746172747357697468685475746f7269616c", bytes.toHexString())
    }

    @Test
    fun stringRoundtripTest() {
        val string = "Hello World!"
        val encoded = Cbor.encode(string)
        val decoded = Cbor.decodeString(encoded)
        assertEquals(string, decoded)
    }

    @Test
    fun encodeArrayInArrayTest() {
        val list = listOf(listOf("one", "two"), listOf("first", "second"))
        val map = hashMapOf(
            "protocolVersion" to ProtocolVersion.latestVersion,
            "listOfLists" to list,
            "identifier" to Entropy.generate()
        )

        val encoded = Cbor.encode(map)

        val decoded = Cbor.decode(encoded)
        assertEquals(map["protocolVersion"], decoded["protocolVersion"])
        assertEquals(map["listOfLists"].toString(), decoded["listOfLists"].toString())
        assertTrue((map["identifier"] as ByteArray).contentEquals(decoded["identifier"] as ByteArray))
    }

    @Test
    fun encodeNullTest() {
        val map = hashMapOf(
            "protocolVersion" to 0,
            "advancedConfig" to null
        )

        val encoded = Cbor.encode(map)

        val decoded = Cbor.decode(encoded)
        assertEquals(map["protocolVersion"], decoded["protocolVersion"])
        assertEquals(null, decoded["advancedConfig"])
    }
}
