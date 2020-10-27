/**
 * Copyright (c) 2018-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashevo.dpp.util

import com.google.common.io.BaseEncoding
import org.bitcoinj.core.Base58
import org.bitcoinj.core.Sha256Hash
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset

object HashUtils {

    private val BASE64 = BaseEncoding.base64()
    private val HEX = BaseEncoding.base16().lowerCase()

    /**
     * Return a double SHA256 hash of a Schema object instance
     * @param obj {object} Schema object instance
     * @returns {*|string}
     */
    private fun toHash(obj: Map<String, Any>): ByteArray {
        val byteArray = Cbor.encode(obj)
        return toHash(byteArray)
    }

    fun toHash(byteArray: ByteArray): ByteArray {
        return Sha256Hash.hashTwice(byteArray)
    }

    fun toSha256Hash(byteArray: ByteArray): Sha256Hash {
        return Sha256Hash.twiceOf(byteArray)
    }

    fun fromBase64(base64: String): ByteArray {
        return BASE64.decode(base64)
    }

    fun fromHex(base16: String): ByteArray {
        return HEX.decode(base16)
    }

    /**
     * Gets a byte array from a string by decoding from one of the
     * following formats: Base58, Base64, hex
     */
    fun byteArrayFromString(string: String): ByteArray {
        return try {
            Base58.decode(string)
        } catch (e: Exception) {
            try {
                fromHex(string)
            } catch (e: Exception) {
                try {
                    fromBase64(string)
                } catch (e: Exception) {
                    throw IllegalArgumentException("string is not base58, base64 or hex: $string")
                }
            }
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

    fun generateDocumentId(dataContractId: String, ownerId: String, type: String, entropy: ByteArray) : String {
        val utf8charset = Charset.forName("UTF-8")
        val stream = ByteArrayOutputStream()
        stream.write(Base58.decode(dataContractId))
        stream.write(Base58.decode(ownerId))
        stream.write(type.toByteArray(utf8charset))
        stream.write(entropy)
        val scopeHash = toHash(stream.toByteArray())
        return Base58.encode(scopeHash)
    }

    fun generateDataContractId(ownerId: String, entropy: ByteArray) : String {
        val stream = ByteArrayOutputStream()
        stream.write(Base58.decode(ownerId))
        stream.write(entropy)
        val scopeHash = toHash(stream.toByteArray())
        return Base58.encode(scopeHash)
    }
}