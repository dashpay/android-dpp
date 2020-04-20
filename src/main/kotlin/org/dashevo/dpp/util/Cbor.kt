package org.dashevo.dpp.util

import co.nstant.`in`.cbor.CborBuilder
import co.nstant.`in`.cbor.CborDecoder
import co.nstant.`in`.cbor.CborEncoder
import co.nstant.`in`.cbor.builder.AbstractBuilder
import co.nstant.`in`.cbor.builder.ArrayBuilder
import co.nstant.`in`.cbor.builder.MapBuilder
import co.nstant.`in`.cbor.model.*
import java.io.ByteArrayOutputStream
import java.lang.IllegalStateException
import java.math.BigInteger
import java.nio.ByteBuffer
import java.util.HashMap

object Cbor {

    fun encode(obj: Map<String, Any?>): ByteArray {
        val baos = ByteArrayOutputStream()
        val cborBuilder = CborBuilder()
        val mapBuilder = writeJSONObject(obj, cborBuilder.addMap(), baos)
        mapBuilder.build()
        CborEncoder(baos).encode(cborBuilder.build())
        return baos.toByteArray()
    }

    fun encode(obj: List<Any>): ByteArray {
        val baos = ByteArrayOutputStream()
        val cborBuilder = CborBuilder()
        val mapBuilder = writeJSONArray(obj, cborBuilder.addArray(), baos)
        mapBuilder.build()
        CborEncoder(baos).encode(cborBuilder.build())
        return baos.toByteArray()
    }


    fun decode(payload: ByteArray): MutableMap<String, Any?> {
        val dataItems = CborDecoder.decode(payload)

        return readJSONObject(dataItems[0] as co.nstant.`in`.cbor.model.Map)
    }

    fun encode(s: String): ByteArray {
        val baos = ByteArrayOutputStream(s.length)
        CborEncoder(baos).encode(UnicodeString(s))
        return baos.toByteArray()
    }

    fun decodeString(bytes: ByteArray): String {
        val decoded = CborDecoder.decode(bytes)
        return decoded[0].toString()
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
                    writeJSONObject(obj[key] as Map<String, Any?>, mapBuilder, baos, innerMapBuilder.putMap(key))
                } else {
                    writeJSONObject(obj[key] as Map<String, Any?>, mapBuilder, baos, mapBuilder.putMap(key))
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

    private fun writeJSONArray(obj: List<Any?>, mapBuilder: ArrayBuilder<CborBuilder>,
                                baos: ByteArrayOutputStream,
                                innerMapBuilder: AbstractBuilder<*>? = null): CborBuilder {

        obj.forEach { value ->
            if (value is Map<*, *>) {
                throw IllegalArgumentException("List contains a map")
            } else {
                val builder: AbstractBuilder<*> = innerMapBuilder ?: mapBuilder
                if (value is List<*>) {
                    if (builder is MapBuilder<*>) {
                        throw IllegalArgumentException("List contains a map")
                    } else if (builder is ArrayBuilder<*>) {
                        addJSONArrayInArray(value, null, baos, builder.addArray())
                    }
                } else if (builder is MapBuilder<*>) {
                    throw IllegalArgumentException("List contains a map")
                } else if (builder is ArrayBuilder<*>) {
                    addValueToArrayBuilder(value, builder)
                }
            }
        }

        return mapBuilder.end()
    }

    /**
     * Writes an array into an array.  This method does not allow maps to be in arrays
     * @param obj List<Any?>
     * @param mapBuilder ArrayBuilder<*>
     * @param baos ByteArrayOutputStream
     * @param innerMapBuilder AbstractBuilder<*>?
     * @return ArrayBuilder<*>
     */
    private fun writeJSONArrayInArray(obj: List<Any?>, mapBuilder: ArrayBuilder<*>,
                                      baos: ByteArrayOutputStream,
                                      innerMapBuilder: AbstractBuilder<*>? = null): ArrayBuilder<*> {

        obj.forEach { value ->
            if (value is Map<*, *>) {
                throw IllegalArgumentException("List contains a map")
            } else {
                val builder: AbstractBuilder<*> = innerMapBuilder ?: mapBuilder
                if (value is List<*>) {
                    if (builder is MapBuilder<*>) {
                        throw IllegalArgumentException("List contains a map")
                    } else if (builder is ArrayBuilder<*>) {
                        addJSONArrayInArray(value, null, baos, builder.addArray())
                    }
                } else if (builder is MapBuilder<*>) {
                    throw IllegalArgumentException("List contains a map")
                } else if (builder is ArrayBuilder<*>) {
                    addValueToArrayBuilder(value, builder)
                }
            }
        }

        return mapBuilder
    }

    /**
     * Adds a JSON array inside a JSON array only.  This does not allow maps to be in arrays
     * @param value List<*> The array to be added
     * @param mapBuilder MapBuilder<CborBuilder>?
     * @param baos ByteArrayOutputStream
     * @param arrayBuilder ArrayBuilder<*> The array will be added to this builder
     */
    private fun addJSONArrayInArray(value: List<*>, mapBuilder: MapBuilder<CborBuilder>?, baos: ByteArrayOutputStream, arrayBuilder: ArrayBuilder<*>) {
        val count = value.size
        for (i in 0 until count) {
            val item = value[i]
            if (item is Map<*, *>) {
                throw IllegalStateException("List contains a map")
            } else if (item is List<*>) {
                writeJSONArrayInArray(item, arrayBuilder.addArray(), baos, null)
            } else {
                addValueToArrayBuilder(item!!, arrayBuilder)
            }
        }
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

    private fun readJSONObject(obj: co.nstant.`in`.cbor.model.Map): MutableMap<String, Any?> {

        val resultMap = HashMap<String, Any?>()
        val sortedKeys = ArrayList<DataItem>()
        sortedKeys.addAll(obj.keys)

        sortedKeys.forEach { key ->
            val keyString = (key as UnicodeString).string
            val value = obj[key]
            if (value is co.nstant.`in`.cbor.model.Map) {
                resultMap[keyString] = readJSONObject(obj[key] as co.nstant.`in`.cbor.model.Map)
            } else {
                if (value is co.nstant.`in`.cbor.model.Array) {
                    resultMap[keyString] = readJSONArray(value)
                } else {
                    if (value != null) {
                        addValueFromCborMap(resultMap, keyString, value)
                    }
                }
            }
        }

        return resultMap
    }

    private fun readJSONArray(value: co.nstant.`in`.cbor.model.Array) : List<Any?> {
        val count = value.dataItems.size
        val resultList = ArrayList<Any?>(count)
        for (i in 0 until count) {
            val item = value.dataItems[i]
            if (item is co.nstant.`in`.cbor.model.Map) {
                resultList.add(readJSONObject(item))
            } else {
                addValueFromCborArray(item, resultList)
            }
        }
        return resultList
    }

    private fun addValueFromCborMap(map: HashMap<String, Any?>, key: String, value: DataItem) {
        when (value) {
            is UnicodeString -> map.put(key, value.string)
            is co.nstant.`in`.cbor.model.Number -> {
                if(value.value.toLong() < Int.MAX_VALUE)
                    map.put(key, value.value.toInt())
                else map[key] = value.value.toLong()
            }
            is HalfPrecisionFloat -> {
                if(value.value.toLong() > Int.MIN_VALUE)
                    map.put(key, value.value.toInt())
                else map[key] = value.value.toLong()
            }
            is NegativeInteger -> map.put(key, value)
            is DoublePrecisionFloat -> map.put(key, value.value)
            is ByteString -> map.put(key, value.bytes)
            is SimpleValue -> {
                when (value.simpleValueType) {
                    SimpleValueType.TRUE -> map[key] = true
                    SimpleValueType.FALSE -> map[key] = false
                    SimpleValueType.NULL -> map[key] = null
                    else -> throw IllegalArgumentException("Unknown simple datatype")
                }
            }
            else -> map.put(key, value.toString()) //?
        }
    }

    private fun addValueFromCborArray(value: DataItem, array: ArrayList<Any?>) {
        when (value) {
            is UnicodeString -> array.add(value.string)
            is co.nstant.`in`.cbor.model.Number -> {
                if(value.value.toLong() < Int.MAX_VALUE)
                    array.add(value.value.intValueExact())
                else array.add(value.value.longValueExact())
            }
            is HalfPrecisionFloat -> {
                if(value.value.toLong() > Int.MIN_VALUE)
                    array.add(value.value.toInt())
                else array.add(value.value.toLong())
            }
            is NegativeInteger -> array.add(value)
            is DoublePrecisionFloat -> array.add(value.value)
            is ByteString -> array.add(value.bytes)
            is SimpleValue -> {
                when (value.simpleValueType) {
                    SimpleValueType.TRUE -> array.add(true)
                    SimpleValueType.FALSE -> array.add(false)
                    SimpleValueType.NULL -> array.add(null)
                    else -> throw IllegalArgumentException("Unknown simple datatype")
                }
            }
            else -> throw java.lang.IllegalArgumentException(value.toString()) //?
        }
    }

}