/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashj.platform.dpp.util

import org.dashj.platform.dpp.ProtocolVersion
import org.dashj.platform.dpp.assertListEquals
import org.dashj.platform.dpp.assertMapEquals
import org.dashj.platform.dpp.identifier.Identifier
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
    fun encodeArrayAndMapsInArrayTest() {
        val list = listOf(
            listOf(
                "one",
                "two",
                listOf(
                    "three",
                    4,
                    mapOf(
                        "strange" to 100,
                        "charm" to 99.0
                    )
                ),
                listOf(
                    true,
                    true,
                    listOf(
                        false,
                        false,
                        mapOf("truth-table" to "TTTFFF")
                    )
                )
            ),
            listOf("first", "second", mapOf("left" to 3.0, "right" to -3.0)),
            mapOf("up" to 1, "down" to 2)
        )

        val encoded = Cbor.encode(list)

        val decoded = Cbor.decodeList(encoded)
        assertListEquals(list, decoded)
    }

    @Test
    fun encodeArrayInArrayInMapTest() {
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
    fun encodeMapInArrayTest() {
        val list = listOf(
            hashMapOf("key" to "value"),
            hashMapOf(
                "anotherKey" to 1,
                "anotherMap" to hashMapOf(
                    "subKey" to 2,
                    "thirdKey" to "three",
                    "subList" to listOf(
                        3,
                        4.5f,
                        5.0,
                        mapOf("null" to null),
                        listOf(15, 16, 17, true, false, null, listOf("a", "b")),
                        Identifier.from(ByteArray(32))
                    )
                )
            ),
            "stringItem"
        )
        val map = hashMapOf(
            "protocolVersion" to ProtocolVersion.latestVersion,
            "listWithMap" to list,
            "identifier" to Entropy.generate()
        )

        val encodedMap = Cbor.encode(map)
        val encodedList = Cbor.encode(list)

        val decoded = Cbor.decode(encodedMap)
        val decodedList = Cbor.decodeList(encodedList)

        assertEquals(map["protocolVersion"], decoded["protocolVersion"])
        assertListEquals(map["listWithMap"] as List<Any>, decoded["listWithMap"] as List<Any>)
        assertTrue((map["identifier"] as ByteArray).contentEquals(decoded["identifier"] as ByteArray))
        assertEquals(3, decodedList.size)
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
        val encodedData = Converters.fromHex(
            "8158a201000000a462696458203ded57a98e85198df73052f6ec3b417dece82d20217a89acb4d6130999e3332b6762616c" +
                "616e63651a3b9ac1cf687265766973696f6e006a7075626c69634b65797381a6626964006464617461582102c9fbb86f8590" +
                "1123191cbfe8ff9f5fe19b28deeb01661955fe45ff615cd27c2f64747970650067707572706f73650068726561644f6e6c79" +
                "f46d73656375726974794c6576656c00"
        )
        val rawData = Converters.fromHex(
            "01000000a462696458203ded57a98e85198df73052f6ec3b417dece82d20217a89acb4d6130999e3332b6762616c616e63" +
                "651a3b9ac1cf687265766973696f6e006a7075626c69634b65797381a6626964006464617461582102c9fbb86f8590112319" +
                "1cbfe8ff9f5fe19b28deeb01661955fe45ff615cd27c2f64747970650067707572706f73650068726561644f6e6c79f46d73" +
                "656375726974794c6576656c00"
        )

        val encoded = Cbor.encode(listOf(rawData))
        assertArrayEquals(encodedData, encoded)
    }

    @Test
    fun decodeListByteArrayTest() {
        val encodedData = Converters.fromHex(
            "8158a201000000a462696458203ded57a98e85198df73052f6ec3b417dece82d20217a89acb4d6130999e3332b676261" +
                "6c616e63651a3b9ac1cf687265766973696f6e006a7075626c69634b65797381a6626964006464617461582102c9fbb86f8" +
                "5901123191cbfe8ff9f5fe19b28deeb01661955fe45ff615cd27c2f64747970650067707572706f73650068726561644f6e" +
                "6c79f46d73656375726974794c6576656c00"
        )
        val rawDataList = listOf(
            Converters.fromHex(
                "01000000a462696458203ded57a98e85198df73052f6ec3b417dece82d20217a89acb4d6130999e3332b6762616c616e" +
                    "63651a3b9ac1cf687265766973696f6e006a7075626c69634b65797381a6626964006464617461582102c9fbb86f8590" +
                    "1123191cbfe8ff9f5fe19b28deeb01661955fe45ff615cd27c2f64747970650067707572706f73650068726561644f6e" +
                    "6c79f46d73656375726974794c6576656c00"
            )
        )

        val decodedData = Cbor.decodeList(encodedData)
        assertListEquals(rawDataList, decodedData)
    }

    @Test
    fun decodeIncorrectFormatTest() {
        val encodedByteArray = Converters.fromHex(
            "58a201000000a462696458203ded57a98e85198df73052f6ec3b417dece82d20217a89acb4d6130999e3332b6762616c" +
                "616e63651a3b9ac1cf687265766973696f6e006a7075626c69634b65797381a6626964006464617461582102c9fbb86f859" +
                "01123191cbfe8ff9f5fe19b28deeb01661955fe45ff615cd27c2f64747970650067707572706f73650068726561644f" +
                "6e6c79f46d73656375726974794c6576656c00"
        )

        val encodedList = Converters.fromHex(
            "8158a201000000a462696458203ded57a98e85198df73052f6ec3b417dece82d20217a89acb4d6130999e3332b676261" +
                "6c616e63651a3b9ac1cf687265766973696f6e006a7075626c69634b65797381a6626964006464617461582102c9fbb86f8" +
                "5901123191cbfe8ff9f5fe19b28deeb01661955fe45ff615cd27c2f64747970650067707572706f73650068726561644f6e" +
                "6c79f46d73656375726974794c6576656c00"
        )

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
        assertThrows<CborDecodeException> {
            Cbor.decodeByteArray(encodedList)
        }
    }

    // a169617267756d656e74738258200e746f7433113dd6794cae9af1381355cb70f09c12903a53cf4c91e63274604a8168246f776e65724964
    @Test
    fun decodeListInsideListTest() {
        val encodedByteArray = Converters.fromHex(
            "a169617267756d656e74738258200e746f7433113dd6794cae9af1381355cb70f09c12903a53cf4c91e63274604a8168246f776e65724964"
        )

        val map = Cbor.decode(encodedByteArray)

        assertTrue(map.containsKey("arguments"))
        val arguments = map["arguments"] as List<Any>
        assertEquals(2, arguments.size)
    }

    @Test
    fun roundTripPrimativesTest() {
        val primativeMap = mapOf(
            "int" to 1,
            "intMax" to Int.MAX_VALUE,
            "negInt" to -1,
            "intMin" to Int.MIN_VALUE,
            "long" to 1L,
            "minLong" to Long.MIN_VALUE,
            "maxLong" to Long.MAX_VALUE,
            "bigLong" to Int.MAX_VALUE + 1L,
            "negBigLong" to Int.MIN_VALUE - 1L,
            "float" to 1.0f,
            "double" to 1.0,
            "bigDouble" to Float.MAX_VALUE * 10.0,
            "string" to "test-value",
            "bytes" to ByteArray(32),
            "true" to true,
            "false" to false,
            "null" to null
        )

        val encoded = Cbor.encode(primativeMap)

        val decodedMap = Cbor.decode(encoded)

        assertMapEquals(primativeMap, decodedMap)

        // check types
        assertTrue(decodedMap["null"] == null)
        assertTrue(decodedMap["int"] is Int)
        assertTrue(decodedMap["negInt"] is Int)
        assertTrue(decodedMap["intMax"] is Int)
        assertTrue(decodedMap["intMin"] is Int)
        assertTrue(decodedMap["long"] is Int) // small numbers are decoded as Int
        assertTrue(decodedMap["bigLong"] is Long)
        assertTrue(decodedMap["minLong"] is Long)
        assertTrue(decodedMap["maxLong"] is Long)
        assertTrue(decodedMap["float"] is Float) // float is preserved by encode/decode
        assertTrue(decodedMap["double"] is Double)
        assertTrue(decodedMap["bigDouble"] is Double)
        assertTrue(decodedMap["string"] is String)
        assertTrue(decodedMap["true"] is Boolean)
        assertTrue(decodedMap["false"] is Boolean)
        assertTrue(decodedMap["bytes"] is ByteArray)
    }

    @Test
    fun roundTripPrimitiveListTest() {
        val primitiveList = listOf<Any>(
            1, // 0
            Int.MAX_VALUE,
            -1,
            Int.MIN_VALUE,
            1L,
            Long.MIN_VALUE, // 5
            Long.MAX_VALUE,
            Int.MAX_VALUE + 1L,
            Int.MIN_VALUE - 1L,
            1.0f,
            1.0, // 10
            Float.MAX_VALUE * 10.0,
            "test-value",
            ByteArray(32),
            true,
            false // 15
        )

        val encoded = Cbor.encode(primitiveList)

        val decodedList = Cbor.decodeList(encoded)

        assertListEquals(primitiveList, decodedList)

        // check types
        assertTrue(decodedList[0] is Int)
        assertTrue(decodedList[1] is Int)
        assertTrue(decodedList[2] is Int)
        assertTrue(decodedList[3] is Int)
        assertTrue(decodedList[4] is Int) // small numbers are decoded as Int
        assertTrue(decodedList[5] is Long)
        assertTrue(decodedList[6] is Long)
        assertTrue(decodedList[7] is Long)
        assertTrue(decodedList[8] is Long) // float is preserved by encode/decode
        assertTrue(decodedList[9] is Float)
        assertTrue(decodedList[10] is Double)
        assertTrue(decodedList[11] is Double)
        assertTrue(decodedList[12] is String)
        assertTrue(decodedList[13] is ByteArray)
        assertTrue(decodedList[14] is Boolean)
        assertTrue(decodedList[15] is Boolean)
    }

    @Test
    fun invalidObjectsTest() {
        data class UnknownClass(val value: Int)

        val mapWithInvalidObject = mapOf("invalidKey" to UnknownClass(1))
        val listWithInvalidObject = listOf(UnknownClass(2))

        assertThrows<IllegalArgumentException> { Cbor.encode(mapWithInvalidObject) }
        assertThrows<IllegalArgumentException> { Cbor.encode(listWithInvalidObject) }
    }
}
