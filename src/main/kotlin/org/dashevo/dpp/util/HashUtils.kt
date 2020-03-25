/**
 * Copyright (c) 2018-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashevo.dpp.util

import co.nstant.`in`.cbor.CborBuilder
import co.nstant.`in`.cbor.CborEncoder
import co.nstant.`in`.cbor.builder.AbstractBuilder
import co.nstant.`in`.cbor.builder.ArrayBuilder
import co.nstant.`in`.cbor.builder.MapBuilder
import co.nstant.`in`.cbor.model.SimpleValue
import co.nstant.`in`.cbor.model.SimpleValueType
import co.nstant.`in`.cbor.model.UnicodeString
import com.google.common.io.BaseEncoding
import org.bitcoinj.core.Sha256Hash
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.nio.ByteBuffer
import java.util.*
import kotlin.collections.ArrayList

object HashUtils {

    fun encode(obj: Map<String, Any?>): ByteArray {
        val baos = ByteArrayOutputStream()
        val cborBuilder = CborBuilder()
        val mapBuilder = writeJSONObject(obj, cborBuilder.addMap(), baos)
        mapBuilder.build()
        CborEncoder(baos).encode(cborBuilder.build())
        return baos.toByteArray()
    }

    /**
     * Return a double SHA256 hash of a Schema object instance
     * @param obj {object} Schema object instance
     * @returns {*|string}
     */
    fun toHash(obj: Map<String, Any>): ByteArray {
        val byteArray = encode(obj)
        return toHash(byteArray)
    }

    fun toHash(byteArray: ByteArray): ByteArray {
        return Sha256Hash.hashTwice(byteArray)
    }

    fun toSha256Hash(byteArray: ByteArray): Sha256Hash {
        return Sha256Hash.twiceOf(byteArray)
    }

    fun fromBase64(base64: String): ByteArray {
        return BaseEncoding.base64().omitPadding().decode(base64)
    }

    fun decode(payload: ByteArray): MutableMap<String, Any?> {
        return decode2(payload)
    }

    private fun writeJSONObject(obj: Map<String, Any?>, mapBuilder: MapBuilder<CborBuilder>,
                                baos: ByteArrayOutputStream,
                                innerMapBuilder: AbstractBuilder<*>? = null): CborBuilder {

        val sortedKeys = ArrayList<String>()
        sortedKeys.addAll(obj.keys)
        sortedKeys.sortWith(Comparator{ a, b ->
            ByteBuffer.wrap(a.toByteArray()).short.compareTo(ByteBuffer.wrap(b.toByteArray()).short)
        })

        sortedKeys.forEach { key ->
            val value = obj.get(key)
            if (value is Map<*, *>) {
                if (innerMapBuilder != null && innerMapBuilder is MapBuilder<*>) {
                    writeJSONObject(obj.get(key) as Map<String, Any?>, mapBuilder, baos, innerMapBuilder.putMap(key))
                } else {
                    writeJSONObject(obj.get(key) as Map<String, Any?>, mapBuilder, baos, mapBuilder.putMap(key))
                }
            } else {
                val builder: AbstractBuilder<*> = innerMapBuilder ?: mapBuilder
                if (value is List<*>) {
                    if (builder is MapBuilder<*>) {
                        addJSONArray(value, mapBuilder, baos, builder.putArray(key))
                    } else if (builder is ArrayBuilder<*>) {
                        addJSONArray(value, mapBuilder, baos, builder.addArray())
                    }
                } else if (builder is MapBuilder<*>) {
                    addValueToMapBuilder(builder, key, value)
                } else if (builder is ArrayBuilder<*>) {
                    addValueToArrayBuilder(value, builder)
                }
            }
        }

        return mapBuilder.end()
    }

    private fun addJSONArray(value: List<*>, mapBuilder: MapBuilder<CborBuilder>, baos: ByteArrayOutputStream, arrayBuilder: ArrayBuilder<*>) {
        val count = value.size
        for (i in 0 until count) {
            val item = value[i]
            if (item is Map<*, *>) {
                writeJSONObject(item as Map<String, Any?>, mapBuilder, baos, arrayBuilder.addMap())
            } else {
                addValueToArrayBuilder(item!!, arrayBuilder)
            }
        }
    }

    private fun addValueToMapBuilder(mapBuilder: MapBuilder<*>, key: String, value: Any?) {
        when (value) {
            is String -> mapBuilder.put(key, value)
            is Boolean -> mapBuilder.put(key, value)
            is Int -> mapBuilder.put(key, value.toLong())
            is BigInteger -> mapBuilder.put(key, value.toLong())
            is Number -> mapBuilder.put(key, value.toLong())
            is Float -> mapBuilder.put(key, value)
            is Long -> mapBuilder.put(key, value)
            is Double -> mapBuilder.put(key, value)
            is ByteArray -> mapBuilder.put(key, value)
            null -> mapBuilder.put(UnicodeString(key), SimpleValue(SimpleValueType.NULL))
            else -> mapBuilder.put(key, value.toString()) //?
        }
    }

    private fun addValueToArrayBuilder(value: Any?, arrayBuilder: ArrayBuilder<*>) {
        when (value) {
            is String -> arrayBuilder.add(value)
            is Boolean -> arrayBuilder.add(value)
            is Int -> arrayBuilder.add(value.toLong())
            is BigInteger -> arrayBuilder.add(value.toLong())
            is Number -> arrayBuilder.add(value.toLong())
            is Float -> arrayBuilder.add(value)
            is Long -> arrayBuilder.add(value)
            is Double -> arrayBuilder.add(value)
            is ByteArray -> arrayBuilder.add(value)
            null -> arrayBuilder.add(SimpleValue(SimpleValueType.NULL))
            else -> arrayBuilder.add(value.toString()) //?
        }
    }

    fun toHash(objList: List<Map<String, Any>>): ByteArray {
        val bos = ByteArrayOutputStream()
        objList.forEach {
            bos.write(Sha256Hash.wrap(toHash(it)).bytes)
        }
        return toHash(bos.toByteArray())
    }

    fun getMerkleTree(hashes: List<ByteArray>): List<ByteArray> {
        val tree = arrayListOf<ByteArray>()
        tree.addAll(hashes.map { it.clone() })

        var j = 0
        var size = hashes.size
        while (size > 1) {
            size = Math.floor((size + 1).toDouble() / 2).toInt()

            var i = 0
            while (i < size) {
                i += 2
                val i2 = Math.min(i + 1, size - 1)
                val a = tree[j + i]
                val b = tree[j + i2]
                val buf = a + b
                tree.add(toHash(buf))
            }

            j += size
        }

        return tree
    }

    fun getMerkleRoot(merkleTree: List<ByteArray>): ByteArray {
        return merkleTree.last().clone()
    }

    @Deprecated ("use Entropy.generate()", ReplaceWith("Entropy.generate()"))
    fun createScopeId(): String {
        return Entropy.generate()
    }

}