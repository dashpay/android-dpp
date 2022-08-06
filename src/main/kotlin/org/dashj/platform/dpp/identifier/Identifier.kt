/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashj.platform.dpp.identifier

import org.bitcoinj.core.Base58
import org.bitcoinj.core.Sha256Hash
import org.dashj.platform.dpp.identifier.errors.IdentifierException
import org.dashj.platform.dpp.toBase64
import org.dashj.platform.dpp.util.Cbor
import org.dashj.platform.dpp.util.Converters
import java.lang.IllegalStateException

data class Identifier(private val buffer: ByteArray) {

    companion object {
        const val MEDIA_TYPE = "application/x.dash.dpp.identifier"
        const val IDENTIFIER_LENGTH = 32

        @JvmStatic
        fun from(any: Any?, encoding: String = "base58"): Identifier {
            return when (any) {
                is String -> {
                    when (encoding) {
                        "base58" -> Identifier(Base58.decode(any))
                        else -> Identifier(Converters.fromBase64(any))
                    }
                }
                is ByteArray -> {
                    Identifier(any)
                }
                is Identifier -> any
                is Sha256Hash -> Identifier(any.bytes)
                else -> throw IllegalStateException("any is not String, ByteArray, Identifier or Sha256Hash")
            }
        }

        @JvmStatic
        fun from(buffer: ByteArray): Identifier {
            return Identifier(buffer)
        }

        @JvmStatic
        fun from(hash: Sha256Hash): Identifier {
            return Identifier(hash.bytes)
        }

        @JvmStatic
        fun fromList(bufferList: List<Any>, encoding: String = "base58"): List<Identifier> {
            return bufferList.map { from(it, encoding) }
        }
    }

    init {
        if (buffer.size != IDENTIFIER_LENGTH) {
            throw IdentifierException("Identifier must be 32 bytes")
        }
    }

    fun toBuffer(): ByteArray {
        return buffer
    }

    fun toSha256Hash(): Sha256Hash {
        return Sha256Hash.wrap(buffer)
    }

    fun encodeCBOR(): ByteArray {
        return Cbor.encode(buffer)
    }

    fun toJSON(): String {
        return toString()
    }

    override fun toString(): String {
        return toString("base58")
    }

    fun toString(encoding: String): String {
        return when (encoding) {
            "base58" -> Base58.encode(buffer)
            else -> buffer.toBase64()
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        return when (other) {
            is Identifier -> buffer.contentEquals(other.buffer)
            is ByteArray -> buffer.contentEquals(other)
            is String -> toString() == other
            is Sha256Hash -> buffer.contentEquals(other.bytes)
            else -> false
        }
    }

    override fun hashCode(): Int {
        return buffer.contentHashCode()
    }
}
