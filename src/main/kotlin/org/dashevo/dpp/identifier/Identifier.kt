/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashevo.dpp.identifier

import org.bitcoinj.core.Base58
import org.dashevo.dpp.identifier.errors.IdentifierError
import org.dashevo.dpp.toBase64
import org.dashevo.dpp.util.Cbor
import org.dashevo.dpp.util.HashUtils
import java.lang.IllegalStateException

data class Identifier(private val buffer: ByteArray) {

    companion object {
        const val MEDIA_TYPE = "application/x.dash.dpp.identifier"

        fun from(any: Any, encoding: String = "base58"): Identifier {
            return when (any) {
                is String -> {
                    when (encoding) {
                        "base58" -> Identifier(Base58.decode(any))
                        else -> Identifier(HashUtils.fromBase64(any))
                    }
                }
                is ByteArray -> {
                    Identifier(any)
                }
                else -> throw IllegalStateException("any is not String or ByteArray")
            }
        }

        fun from(buffer: ByteArray): Identifier {
            return Identifier(buffer)
        }
    }

    init {
        if (buffer.size != 32) {
            throw IdentifierError("Identifier must be 32 bytes")
        }
    }

    fun toBuffer(): ByteArray {
        return buffer
    }

    fun encodeCBOR() : ByteArray {
        return Cbor.encode(buffer)
    }

    fun toJSON(): String {
        return toString()
    }

    fun toString(encoding: String = "base58"): String {
        return when(encoding) {
            "base58" -> Base58.encode(buffer)
            else -> buffer.toBase64()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Identifier

        if (!buffer.contentEquals(other.buffer)) return false

        return true
    }

    override fun hashCode(): Int {
        return buffer.contentHashCode()
    }
}