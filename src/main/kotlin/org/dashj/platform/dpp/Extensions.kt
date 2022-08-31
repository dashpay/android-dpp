/**
 * Copyright (c) 2018-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashj.platform.dpp

import com.google.common.io.BaseEncoding
import org.bitcoinj.core.AddressFormatException
import org.bitcoinj.core.Base58
import org.bitcoinj.core.Sha256Hash
import org.dashj.platform.dpp.util.Converters
import org.json.JSONObject

fun JSONObject.append(jsonObject: JSONObject): JSONObject {
    jsonObject.keys().forEach {
        this.put(it, jsonObject[it])
    }
    return this
}

/**
 *
 * @receiver ByteArray
 * @return String The HEX or Base16 representation of this
 */
fun ByteArray.toHex(): String {
    return this.joinToString("") {
        java.lang.String.format("%02x", it)
    }
}
@Deprecated("Use toHex", replaceWith = ReplaceWith("toHex()", "org.dashj.platform.dpp.util"))
fun ByteArray.toHexString(): String {
    return toHex()
}

/**
 *
 * @receiver ByteArray
 * @return String The Base64 representation of this
 */
fun ByteArray.toBase64(): String {
    return BaseEncoding.base64().omitPadding().encode(this)
}

/**
 * @receiver ByteArray
 * @return String The Base64 representation with padding of this
 */
fun ByteArray.toBase64Padded(): String {
    return BaseEncoding.base64().encode(this)
}

/**
 *
 * @receiver ByteArray
 * @return String The Base58 representation of this
 */
fun ByteArray.toBase58(): String {
    return Base58.encode(this)
}

fun ByteArray.toSha256Hash(): Sha256Hash {
    return Sha256Hash.twiceOf(this)
}

fun ByteArray.hashTwice(): ByteArray {
    return Sha256Hash.hashTwice(this)
}

fun ByteArray.hashOnce(): ByteArray {
    return Sha256Hash.hash(this)
}

fun String.toByteArray(): ByteArray {
    return try {
        Base58.decode(this)
    } catch (e: AddressFormatException) {
        try {
            Converters.fromHex(this)
        } catch (e: IllegalArgumentException) {
            try {
                Converters.fromBase64(this)
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("string is not base58, base64 or hex: $this", e)
            }
        }
    }
}

fun List<Any>.deepCopy(): List<Any> {
    val copy = arrayListOf<Any>()
    copy.addAll(this)
    return copy
}

fun Map<String, Any?>.deepCopy(): Map<String, Any?> {
    val copy = HashMap<String, Any?>(size)
    for (key in keys) {
        when (val value = get(key)) {
            is Map<*, *> -> copy[key] = (value as Map<String, Any?>).deepCopy()
            is List<*> -> copy[key] = (value as List<Any>).deepCopy()
            else -> copy[key] = value
        }
    }
    return copy
}

// during CBOR encoding, Long and Int are lost, but we would like for them to be considered equal if
// they have the same value
fun List<Any>.deepCompare(list: List<Any>): Boolean {
    if (list.size != size)
        return false
    var equals = true
    for (i in indices) {
        val thisValue = get(i)
        val value = list[i]
        equals = when {
            value is ByteArray && thisValue is ByteArray -> thisValue.contentEquals(value)
            thisValue is Map<*, *> && value is Map<*, *> ->
                (thisValue as Map<String, Any?>).deepCompare(value as Map<String, Any?>)
            thisValue is List<*> && value is List<*> -> (thisValue as List<Any>).deepCompare(value as List<Any>)
            thisValue is Int && value is Long -> thisValue.toLong() == value
            thisValue is Long && value is Int -> thisValue == value.toLong()
            else -> thisValue == value
        }
        if (!equals) {
            break
        }
    }
    return equals
}

fun Map<String, Any?>.deepCompare(map: Map<String, Any?>): Boolean {
    if (map.size != size || map.keys != keys) {
        return false
    }
    var equals = true
    for (key in keys) {
        val thisValue = get(key)
        val value = map[key]
        equals = when {
            thisValue == null -> value == null
            value == null -> false
            value is ByteArray && thisValue is ByteArray -> thisValue.contentEquals(value)
            thisValue is Map<*, *> && value is Map<*, *> ->
                (thisValue as Map<String, Any?>).deepCompare(value as Map<String, Any?>)
            thisValue is List<*> && value is List<*> -> (thisValue as List<Any>).deepCompare(value as List<Any>)
            thisValue is Int && value is Long -> thisValue.toLong() == value
            thisValue is Long && value is Int -> thisValue == value.toLong()
            else -> thisValue == value
        }
        if (!equals) {
            break
        }
    }
    return equals
}
