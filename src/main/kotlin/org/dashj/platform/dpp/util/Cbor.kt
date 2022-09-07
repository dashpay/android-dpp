/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashj.platform.dpp.util

import co.nstant.`in`.cbor.CborBuilder
import co.nstant.`in`.cbor.CborDecoder
import co.nstant.`in`.cbor.CborEncoder
import co.nstant.`in`.cbor.builder.ArrayBuilder
import co.nstant.`in`.cbor.builder.MapBuilder
import co.nstant.`in`.cbor.model.Array
import co.nstant.`in`.cbor.model.ByteString
import co.nstant.`in`.cbor.model.DataItem
import co.nstant.`in`.cbor.model.DoublePrecisionFloat
import co.nstant.`in`.cbor.model.HalfPrecisionFloat
import co.nstant.`in`.cbor.model.NegativeInteger
import co.nstant.`in`.cbor.model.SimpleValue
import co.nstant.`in`.cbor.model.SimpleValueType
import co.nstant.`in`.cbor.model.UnicodeString
import co.nstant.`in`.cbor.model.UnsignedInteger
import org.dashj.platform.dpp.identifier.Identifier
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.nio.ByteBuffer
import java.util.HashMap

/**
 * @author Sam Barbosa
 * @author Eric Britten
 */
/**
 * Cbor is a utility class that converts Map<String, Any?> and List<Any> to CBOR format and vice versa.
 */
object Cbor {

    fun encode(obj: Map<String, Any?>): ByteArray {
        val baos = ByteArrayOutputStream()
        val cborBuilder = CborBuilder()
        val mapBuilder = writeRootJSONObject(obj, cborBuilder.addMap())
        mapBuilder.build()
        CborEncoder(baos).encode(cborBuilder.build())
        return baos.toByteArray()
    }

    fun encode(obj: List<Any>): ByteArray {
        val baos = ByteArrayOutputStream()
        val cborBuilder = CborBuilder()
        val arrayBuilder = writeRootJSONArray(obj, cborBuilder.addArray())
        arrayBuilder.build()
        CborEncoder(baos).encode(cborBuilder.build())
        return baos.toByteArray()
    }

    /**
     * decode a CBOR byte array as a Map
     */

    fun decode(payload: ByteArray): MutableMap<String, Any?> {
        try {
            val dataItems = CborDecoder.decode(payload)
            return readJSONObject(dataItems[0] as co.nstant.`in`.cbor.model.Map)
        } catch (e: ClassCastException) {
            throw CborDecodeException("payload is not a map", e)
        }
    }

    fun encode(s: String): ByteArray {
        val baos = ByteArrayOutputStream(s.length)
        CborEncoder(baos).encode(UnicodeString(s))
        return baos.toByteArray()
    }

    fun encode(payload: ByteArray): ByteArray {
        val baos = ByteArrayOutputStream(payload.size)
        CborEncoder(baos).encode(ByteString(payload))
        return baos.toByteArray()
    }

    /**
     * decode a CBOR byte array as a String
     */

    fun decodeString(payload: ByteArray): String {
        try {
            val decoded = CborDecoder.decode(payload)
            return (decoded[0] as UnicodeString).toString()
        } catch (e: ClassCastException) {
            throw CborDecodeException("payload is not a String", e)
        }
    }

    /**
     * decode a CBOR byte array as a ByteArray
     */

    fun decodeByteArray(payload: ByteArray): ByteArray {
        try {
            val decoded = CborDecoder.decode(payload)
            return (decoded[0] as ByteString).bytes
        } catch (e: ClassCastException) {
            throw CborDecodeException("payload is not a ByteArray", e)
        }
    }

    /**
     * decode a CBOR byte array as a List
     */

    fun decodeList(payload: ByteArray): List<Any> {
        try {
            val dataItems = CborDecoder.decode(payload)
            return readJSONArray(dataItems[0] as Array).filterNotNull()
        } catch (e: ClassCastException) {
            throw CborDecodeException("payload is not a List<Any>", e)
        }
    }

    private fun writeRootJSONObject(
        obj: Map<String, Any?>,
        mapBuilder: MapBuilder<CborBuilder>
    ): CborBuilder {
        writeJSONObject(obj, mapBuilder)
        return mapBuilder.end()
    }

    private fun writeJSONObject(
        obj: Map<String, Any?>,
        builder: MapBuilder<*>
    ) {
        val sortedKeys = ArrayList<String>()
        sortedKeys.addAll(obj.keys)
        sortedKeys.sortWith { a, b ->
            ByteBuffer.wrap(a.toByteArray()).short.compareTo(ByteBuffer.wrap(b.toByteArray()).short)
        }

        sortedKeys.forEach { key ->
            val value = obj[key]
            when (value) {
                is Map<*, *> -> writeJSONObject(obj[key] as Map<String, Any?>, builder.putMap(key))
                is List<*> -> writeJSONArray(value, builder.putArray(key))
                else -> addValueToMapBuilder(builder, key, value)
            }
        }
        builder.end()
    }

    private fun writeRootJSONArray(
        obj: List<Any?>,
        arrayBuilder: ArrayBuilder<CborBuilder>
    ): CborBuilder {
        writeJSONArray(obj, arrayBuilder)
        return arrayBuilder.end()
    }

    private fun writeJSONArray(
        obj: List<Any?>,
        builder: ArrayBuilder<*>
    ) {
        obj.forEach { value ->
            when (value) {
                is Map<*, *> -> writeJSONObject(value as Map<String, Any?>, builder.addMap())
                is List<*> -> writeJSONArray(value, builder.addArray())
                else -> addValueToArrayBuilder(value, builder)
            }
        }
        builder.end()
    }

    private fun addValueToMapBuilder(mapBuilder: MapBuilder<*>, key: String, value: Any?) {
        when (value) {
            is String -> mapBuilder.put(key, value)
            is Boolean -> mapBuilder.put(key, value)
            is Int -> mapBuilder.put(key, value.toLong())
            is BigInteger -> mapBuilder.put(key, value.toLong())
            is Float -> mapBuilder.put(key, value)
            is Long -> mapBuilder.put(key, value)
            is Double -> mapBuilder.put(key, value)
            is ByteArray -> mapBuilder.put(key, value)
            is Identifier -> mapBuilder.put(key, value.toBuffer())
            is Number -> mapBuilder.put(key, value.toLong())
            null -> mapBuilder.put(UnicodeString(key), SimpleValue(SimpleValueType.NULL))
            else -> throw IllegalArgumentException("No converter for $value)")
        }
    }

    private fun addValueToArrayBuilder(value: Any?, arrayBuilder: ArrayBuilder<*>) {
        when (value) {
            is String -> arrayBuilder.add(value)
            is Boolean -> arrayBuilder.add(value)
            is Int -> arrayBuilder.add(value.toLong())
            is BigInteger -> arrayBuilder.add(value.toLong())
            is Float -> arrayBuilder.add(value)
            is Long -> arrayBuilder.add(value)
            is Double -> arrayBuilder.add(value)
            is ByteArray -> arrayBuilder.add(value)
            is Identifier -> arrayBuilder.add(value.toBuffer())
            is Number -> arrayBuilder.add(value.toLong())
            null -> arrayBuilder.add(SimpleValue(SimpleValueType.NULL))
            else -> throw IllegalArgumentException("No converter for $value")
        }
    }

    private fun readJSONObject(obj: co.nstant.`in`.cbor.model.Map): MutableMap<String, Any?> {
        val resultMap = HashMap<String, Any?>()
        val keys = ArrayList<DataItem>()
        keys.addAll(obj.keys)

        keys.forEach { key ->
            val keyString = (key as UnicodeString).string
            val value = obj[key]
            when (value) {
                is co.nstant.`in`.cbor.model.Map -> {
                    resultMap[keyString] = readJSONObject(obj[key] as co.nstant.`in`.cbor.model.Map)
                }
                is Array -> resultMap[keyString] = readJSONArray(value)
                else -> addValueFromCborMap(resultMap, keyString, value)
            }
        }

        return resultMap
    }

    private fun readJSONArray(value: Array): List<Any?> {
        val count = value.dataItems.size
        val resultList = ArrayList<Any?>(count)
        for (i in 0 until count) {
            when (val item = value.dataItems[i]) {
                is co.nstant.`in`.cbor.model.Map -> resultList.add(readJSONObject(item))
                is Array -> resultList.add(readJSONArray(item))
                else -> addValueFromCborArray(item, resultList)
            }
        }
        return resultList
    }

    private fun addValueFromCborMap(map: HashMap<String, Any?>, key: String, value: DataItem) {
        when (value) {
            is UnicodeString -> {
                map[key] = value.string
            }
            is NegativeInteger -> {
                if (value.value.toLong() >= Int.MIN_VALUE) {
                    map[key] = value.value.toInt()
                } else {
                    map[key] = value.value.toLong()
                }
            }
            is UnsignedInteger -> {
                if (value.value.toLong() <= Int.MAX_VALUE) {
                    map[key] = value.value.toInt()
                } else {
                    map[key] = value.value.toLong()
                }
            }
            is HalfPrecisionFloat -> map[key] = value.value
            is DoublePrecisionFloat -> map[key] = value.value
            is ByteString -> map[key] = value.bytes
            is SimpleValue -> {
                when (value.simpleValueType) {
                    SimpleValueType.TRUE -> map[key] = true
                    SimpleValueType.FALSE -> map[key] = false
                    SimpleValueType.NULL -> map[key] = null
                    SimpleValueType.UNDEFINED -> map[key] = "undefined"
                    else -> throw IllegalArgumentException("Unknown simple datatype")
                }
            }
            else -> throw java.lang.IllegalArgumentException(value.toString()) // the type is not known
        }
    }

    private fun addValueFromCborArray(value: DataItem, array: ArrayList<Any?>) {
        when (value) {
            is UnicodeString -> array.add(value.string)
            is NegativeInteger -> {
                if (value.value.toLong() >= Int.MIN_VALUE) {
                    array.add(value.value.toLong().toInt())
                } else {
                    array.add(value.value.toLong())
                }
            }
            is UnsignedInteger -> {
                if (value.value.toLong() <= Int.MAX_VALUE) {
                    array.add(value.value.toLong().toInt())
                } else {
                    array.add(value.value.toLong())
                }
            }
            is HalfPrecisionFloat -> array.add(value.value)
            is DoublePrecisionFloat -> array.add(value.value)
            is ByteString -> array.add(value.bytes)
            is SimpleValue -> {
                when (value.simpleValueType) {
                    SimpleValueType.TRUE -> array.add(true)
                    SimpleValueType.FALSE -> array.add(false)
                    SimpleValueType.NULL -> array.add(null)
                    SimpleValueType.UNDEFINED -> array.add("undefined")
                    else -> throw IllegalArgumentException("Unknown simple datatype")
                }
            }
            else -> throw java.lang.IllegalArgumentException(value.toString()) // the type is not known
        }
    }
}
