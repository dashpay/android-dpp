/**
 * Copyright (c) 2018-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashj.platform.dpp.util

import org.bitcoinj.core.Sha256Hash
import org.dashj.platform.dpp.hashTwice
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import kotlin.math.floor

object HashUtils {

    /**
     * Return a double SHA256 hash of a Schema object instance
     * @param obj {object} Schema object instance
     * @returns {*|string}
     */
    private fun toHash(obj: Map<String, Any>): ByteArray {
        val byteArray = Cbor.encode(obj)
        return byteArray.hashTwice()
    }

    fun toHash(byteArray: ByteArray): ByteArray {
        return Sha256Hash.hashTwice(byteArray)
    }

    fun toSha256Hash(byteArray: ByteArray): Sha256Hash {
        return Sha256Hash.twiceOf(byteArray)
    }

    @Deprecated("use the same method from the Converters object")
    fun fromBase64(base64: String): ByteArray {
        return Converters.fromBase64(base64)
    }

    @Deprecated("use the same method from the Converters object")
    fun fromHex(base16: String): ByteArray {
        return Converters.fromHex(base16)
    }

    @Deprecated("use the same method from the Converters object")
    fun byteArrayfromBase64orByteArray(any: Any): ByteArray {
        return Converters.byteArrayFromBase64orByteArray(any)
    }

    @Deprecated("use the same method from the Converters object")
    fun byteArrayFromBase64orByteArray(any: Any): ByteArray {
        return Converters.byteArrayFromBase64orByteArray(any)
    }

    @Deprecated("use the same method from the Converters object")
    fun byteArrayfromBase58orByteArray(any: Any): ByteArray {
        return Converters.byteArrayFromBase58orByteArray(any)
    }

    @Deprecated("use the same method from the Converters object")
    fun byteArrayFromBase58orByteArray(any: Any): ByteArray {
        return Converters.byteArrayFromBase58orByteArray(any)
    }

    /**
     * Gets a byte array from a string by decoding from one of the
     * following formats: Base58, Base64, hex
     */
    @Deprecated("use the same method from the Converters object")
    fun byteArrayFromString(string: String): ByteArray {
        return Converters.byteArrayFromString(string)
    }

    fun toHash(objList: List<Map<String, Any>>): ByteArray {
        val bos = ByteArrayOutputStream()
        objList.forEach {
            bos.write(Sha256Hash.wrap(toHash(it)).bytes)
        }
        return bos.toByteArray().hashTwice()
    }

    fun getMerkleTree(hashes: List<ByteArray>): List<ByteArray> {
        val tree = arrayListOf<ByteArray>()
        tree.addAll(hashes.map { it.clone() })

        var j = 0
        var size = hashes.size
        while (size > 1) {
            size = floor((size + 1).toDouble() / 2).toInt()

            var i = 0
            while (i < size) {
                i += 2
                val i2 = (i + 1).coerceAtMost(size - 1)
                val a = tree[j + i]
                val b = tree[j + i2]
                val buf = a + b
                tree.add(buf.hashTwice())
            }

            j += size
        }

        return tree
    }

    fun getMerkleRoot(merkleTree: List<ByteArray>): ByteArray {
        return merkleTree.last().clone()
    }

    fun generateDocumentId(dataContractId: ByteArray, ownerId: ByteArray, type: String, entropy: ByteArray): ByteArray {
        val utf8charset = Charset.forName("UTF-8")
        val stream = ByteArrayOutputStream()
        stream.write(dataContractId)
        stream.write(ownerId)
        stream.write(type.toByteArray(utf8charset))
        stream.write(entropy)
        return stream.toByteArray().hashTwice()
    }

    fun generateDataContractId(ownerId: ByteArray, entropy: ByteArray): ByteArray {
        val stream = ByteArrayOutputStream()
        stream.write(ownerId)
        stream.write(entropy)
        return stream.toByteArray().hashTwice()
    }
}
