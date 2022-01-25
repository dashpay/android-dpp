/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashj.platform.dpp.util

import org.dashj.platform.dpp.ProtocolVersion
import org.dashj.platform.dpp.assertListEquals
import org.dashj.platform.dpp.toHexString
import org.json.JSONArray
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

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

    @Test
    fun encodeByteArray() {
        val encodedData = Converters.fromHex("58a201000000a462696458203ded57a98e85198df73052f6ec3b417dece82d20217a89acb4d6130999e3332b6762616c616e63651a3b9ac1cf687265766973696f6e006a7075626c69634b65797381a6626964006464617461582102c9fbb86f85901123191cbfe8ff9f5fe19b28deeb01661955fe45ff615cd27c2f64747970650067707572706f73650068726561644f6e6c79f46d73656375726974794c6576656c00")
        val rawData = Converters.fromHex("01000000a462696458203ded57a98e85198df73052f6ec3b417dece82d20217a89acb4d6130999e3332b6762616c616e63651a3b9ac1cf687265766973696f6e006a7075626c69634b65797381a6626964006464617461582102c9fbb86f85901123191cbfe8ff9f5fe19b28deeb01661955fe45ff615cd27c2f64747970650067707572706f73650068726561644f6e6c79f46d73656375726974794c6576656c00")

        val encoded = Cbor.encode(rawData)
        assertArrayEquals(encodedData, encoded)
    }

    @Test
    fun decodeByteArrayTest() {
        val encodedData = Converters.fromHex("58a201000000a462696458203ded57a98e85198df73052f6ec3b417dece82d20217a89acb4d6130999e3332b6762616c616e63651a3b9ac1cf687265766973696f6e006a7075626c69634b65797381a6626964006464617461582102c9fbb86f85901123191cbfe8ff9f5fe19b28deeb01661955fe45ff615cd27c2f64747970650067707572706f73650068726561644f6e6c79f46d73656375726974794c6576656c00")
        val rawData = Converters.fromHex("01000000a462696458203ded57a98e85198df73052f6ec3b417dece82d20217a89acb4d6130999e3332b6762616c616e63651a3b9ac1cf687265766973696f6e006a7075626c69634b65797381a6626964006464617461582102c9fbb86f85901123191cbfe8ff9f5fe19b28deeb01661955fe45ff615cd27c2f64747970650067707572706f73650068726561644f6e6c79f46d73656375726974794c6576656c00")

        val decodedData = Cbor.decodeByteArray(encodedData)
        assertArrayEquals(rawData, decodedData)
    }

    @Test
    fun encodeListByteArrayTest() {
        val encodedData = Converters.fromHex("8158a201000000a462696458203ded57a98e85198df73052f6ec3b417dece82d20217a89acb4d6130999e3332b6762616c616e63651a3b9ac1cf687265766973696f6e006a7075626c69634b65797381a6626964006464617461582102c9fbb86f85901123191cbfe8ff9f5fe19b28deeb01661955fe45ff615cd27c2f64747970650067707572706f73650068726561644f6e6c79f46d73656375726974794c6576656c00")
        val rawData = Converters.fromHex("01000000a462696458203ded57a98e85198df73052f6ec3b417dece82d20217a89acb4d6130999e3332b6762616c616e63651a3b9ac1cf687265766973696f6e006a7075626c69634b65797381a6626964006464617461582102c9fbb86f85901123191cbfe8ff9f5fe19b28deeb01661955fe45ff615cd27c2f64747970650067707572706f73650068726561644f6e6c79f46d73656375726974794c6576656c00")

        val encoded = Cbor.encode(listOf(rawData))
        assertArrayEquals(encodedData, encoded)
    }

    @Test
    fun decodeListByteArrayTest() {
        val encodedData = Converters.fromHex("8158a201000000a462696458203ded57a98e85198df73052f6ec3b417dece82d20217a89acb4d6130999e3332b6762616c616e63651a3b9ac1cf687265766973696f6e006a7075626c69634b65797381a6626964006464617461582102c9fbb86f85901123191cbfe8ff9f5fe19b28deeb01661955fe45ff615cd27c2f64747970650067707572706f73650068726561644f6e6c79f46d73656375726974794c6576656c00")
        val rawDataList = listOf(
            Converters.fromHex("01000000a462696458203ded57a98e85198df73052f6ec3b417dece82d20217a89acb4d6130999e3332b6762616c616e63651a3b9ac1cf687265766973696f6e006a7075626c69634b65797381a6626964006464617461582102c9fbb86f85901123191cbfe8ff9f5fe19b28deeb01661955fe45ff615cd27c2f64747970650067707572706f73650068726561644f6e6c79f46d73656375726974794c6576656c00")
        )

        val decodedData = Cbor.decodeList(encodedData)
        assertListEquals(rawDataList, decodedData)
    }

    @Test
    fun decodeIncorrectFormatTest() {
        val encodedByteArray = Converters.fromHex("58a201000000a462696458203ded57a98e85198df73052f6ec3b417dece82d20217a89acb4d6130999e3332b6762616c616e63651a3b9ac1cf687265766973696f6e006a7075626c69634b65797381a6626964006464617461582102c9fbb86f85901123191cbfe8ff9f5fe19b28deeb01661955fe45ff615cd27c2f64747970650067707572706f73650068726561644f6e6c79f46d73656375726974794c6576656c00")

        // this should not throw an exception
        Cbor.decodeByteArray(encodedByteArray)

        assertThrows<CborDecodeException> {
            Cbor.decode(encodedByteArray)
        }
        assertThrows<CborDecodeException> {
            Cbor.decodeString(encodedByteArray)
        }
        assertThrows<CborDecodeException> {
            Cbor.decodeList(encodedByteArray)
        }
    }
}
