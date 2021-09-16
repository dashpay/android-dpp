/**
 * Copyright (c) 2021-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashj.platform.dpp.util

import com.google.common.io.BaseEncoding
import org.bitcoinj.core.AddressFormatException
import org.bitcoinj.core.Base58
import org.dashj.platform.dpp.identifier.Identifier
import org.dashj.platform.dpp.toBase64

object Converters {

    private val BASE64: BaseEncoding = BaseEncoding.base64()
    private val HEX: BaseEncoding = BaseEncoding.base16().lowerCase()

    @JvmStatic
    fun fromBase64(base64: String): ByteArray {
        return BASE64.decode(base64)
    }

    @JvmStatic
    fun fromBase64Padded(base64Padded: String): ByteArray {
        return BaseEncoding.base64().decode(base64Padded)
    }

    @JvmStatic
    fun fromHex(base16: String): ByteArray {
        return HEX.decode(base16)
    }

    @Deprecated("from should be From")
    fun byteArrayfromBase64orByteArray(any: Any): ByteArray {
        return byteArrayFromBase64orByteArray(any)
    }

    @JvmStatic
    fun byteArrayFromBase64orByteArray(any: Any): ByteArray {
        return when (any) {
            is String -> {
                fromBase64(any)
            }
            is ByteArray -> any
            else -> throw IllegalStateException("any is not String or ByteArray")
        }
    }

    @Deprecated("from should be From")
    fun byteArrayfromBase58orByteArray(any: Any): ByteArray {
        return byteArrayFromBase58orByteArray(any)
    }

    @JvmStatic
    fun byteArrayFromBase58orByteArray(any: Any): ByteArray {
        return when (any) {
            is String -> {
                Base58.decode(any)
            }
            is ByteArray -> any
            else -> throw IllegalStateException("any is not String or ByteArray")
        }
    }

    /**
     * Gets a byte array from a string by decoding from one of the
     * following formats: Base58, Base64, hex
     */
    @JvmStatic
    fun byteArrayFromString(string: String): ByteArray {
        return try {
            Base58.decode(string)
        } catch (e: AddressFormatException) {
            try {
                fromHex(string)
            } catch (e: IllegalArgumentException) {
                try {
                    fromBase64(string)
                } catch (e: IllegalArgumentException) {
                    throw IllegalArgumentException("string is not base58, base64 or hex: $string", e)
                }
            }
        }
    }

    @JvmStatic
    fun convertDataToString(map: MutableMap<String, Any?>) {
        for (key in map.keys) {
            when (val value = map[key]) {
                is Map<*, *> -> convertDataToString(value as MutableMap<String, Any?>)
                is ByteArray -> map[key] = value.toBase64()
                is Identifier -> map[key] = value.toString()
            }
        }
    }

    @JvmStatic
    fun convertIdentifierToByteArray(map: MutableMap<String, Any?>) {
        for (key in map.keys) {
            when (val value = map[key]) {
                is Map<*, *> -> convertIdentifierToByteArray(value as MutableMap<String, Any?>)
                is Identifier -> map[key] = value.toBuffer()
            }
        }
    }
}
