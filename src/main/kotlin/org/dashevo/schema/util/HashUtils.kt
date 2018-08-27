package org.dashevo.schema.util

import co.nstant.`in`.cbor.CborBuilder
import co.nstant.`in`.cbor.CborEncoder
import co.nstant.`in`.cbor.builder.AbstractBuilder
import co.nstant.`in`.cbor.builder.ArrayBuilder
import co.nstant.`in`.cbor.builder.MapBuilder
import org.bitcoinj.core.Sha256Hash
import org.jsonorg.JSONArray
import org.jsonorg.JSONObject
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.nio.ByteBuffer
import java.util.*

object HashUtils {

    fun encode(obj: JSONObject): ByteArray {
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
    fun toHash(obj: JSONObject): String {
        val byteArray = encode(obj)
        return Sha256Hash.wrapReversed(Sha256Hash.hashTwice(byteArray)).toString()
    }

    private fun writeJSONObject(obj: JSONObject, mapBuilder: MapBuilder<CborBuilder>,
                                baos: ByteArrayOutputStream,
                                innerMapBuilder: AbstractBuilder<*>? = null): CborBuilder {

        val sortedKeys = ArrayList<String>()
        sortedKeys.addAll(obj.keySet())
        sortedKeys.sortWith(Comparator{ a, b ->
            ByteBuffer.wrap(a.toByteArray()).short.compareTo(ByteBuffer.wrap(b.toByteArray()).short)
        })

        sortedKeys.forEach { key ->
            val value = obj.get(key)
            if (value is JSONObject) {
                if (innerMapBuilder != null && innerMapBuilder is MapBuilder<*>) {
                    writeJSONObject(obj.getJSONObject(key), mapBuilder, baos, innerMapBuilder.putMap(key))
                } else {
                    writeJSONObject(obj.getJSONObject(key), mapBuilder, baos, mapBuilder.putMap(key))
                }
            } else {
                val builder: AbstractBuilder<*> = innerMapBuilder ?: mapBuilder
                if (value is JSONArray) {
                    if (builder is MapBuilder<*>) {
                        addJSONArray(value, mapBuilder, baos, builder.putArray(key))
                    } else if (builder is ArrayBuilder<*>) {
                        addJSONArray(value, mapBuilder, baos, builder.addArray())
                    }
                } else if (builder is MapBuilder<*>){
                    addValueToMapBuilder(builder, key, value)
                } else if (builder is ArrayBuilder<*>) {
                    addValueToArrayBuilder(value, builder)
                }
            }
        }

        return mapBuilder.end()
    }

    private fun addJSONArray(value: JSONArray, mapBuilder: MapBuilder<CborBuilder>, baos: ByteArrayOutputStream, arrayBuilder: ArrayBuilder<*>) {
        val count = value.length()
        for (i in 0 until count) {
            val item = value[i]
            if (item is JSONObject) {
                writeJSONObject(item, mapBuilder, baos, arrayBuilder.addMap())
            } else {
                addValueToArrayBuilder(item, arrayBuilder)
            }
        }
    }

    private fun addValueToMapBuilder(mapBuilder: MapBuilder<*>, key: String, value: Any) {
        when (value) {
            is String -> mapBuilder.put(key, value)
            is Boolean -> mapBuilder.put(key, value)
            is Int -> mapBuilder.put(key, value.toLong())
            is BigInteger -> mapBuilder.put(key, value.toLong())
            is Number -> mapBuilder.put(key, value.toLong())
            is Float -> mapBuilder.put(key, value)
            is Long -> mapBuilder.put(key, value)
            is Double -> mapBuilder.put(key, value)
            else -> mapBuilder.put(key, value.toString()) //?
        }
    }

    private fun addValueToArrayBuilder(value: Any, arrayBuilder: ArrayBuilder<*>) {
        when (value) {
            is String -> arrayBuilder.add(value)
            is Boolean -> arrayBuilder.add(value)
            is Int -> arrayBuilder.add(value.toLong())
            is BigInteger -> arrayBuilder.add(value.toLong())
            is Number -> arrayBuilder.add(value.toLong())
            is Float -> arrayBuilder.add(value)
            is Long -> arrayBuilder.add(value)
            is Double -> arrayBuilder.add(value)
            else -> arrayBuilder.add(value.toString()) //?
        }
    }

    fun toHash(objList: List<JSONObject>): String {
        val bos = ByteArrayOutputStream()
        objList.forEach {
            bos.write(Sha256Hash.wrap(toHash(it)).bytes)
        }
        return Sha256Hash.hashTwice(bos.toByteArray()).toString()
    }

}