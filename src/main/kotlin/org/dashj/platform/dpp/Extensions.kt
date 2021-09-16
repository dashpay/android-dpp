/**
 * Copyright (c) 2018-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashj.platform.dpp

import com.google.common.io.BaseEncoding
import org.bitcoinj.core.Base58
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
fun ByteArray.toHexString(): String {
    return this.joinToString("") {
        java.lang.String.format("%02x", it)
    }
}
@Deprecated("Use toHex", replaceWith = ReplaceWith("toHex replaces toHexString", "org.dashj.platform.dpp.util"))
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
