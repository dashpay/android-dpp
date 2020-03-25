/**
 * Copyright (c) 2018-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashevo.dpp.util

import co.nstant.`in`.cbor.CborBuilder
import co.nstant.`in`.cbor.CborDecoder
import co.nstant.`in`.cbor.CborEncoder
import co.nstant.`in`.cbor.builder.AbstractBuilder
import co.nstant.`in`.cbor.builder.ArrayBuilder
import co.nstant.`in`.cbor.builder.MapBuilder
import co.nstant.`in`.cbor.model.*
import com.google.common.io.BaseEncoding
import org.bitcoinj.core.Sha256Hash
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.nio.ByteBuffer
import java.util.*
import kotlin.collections.ArrayList

object HashUtils {

    /**
     * Return a double SHA256 hash of a Schema object instance
     * @param obj {object} Schema object instance
     * @returns {*|string}
     */
    fun toHash(obj: Map<String, Any>): ByteArray {
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
        return BaseEncoding.base64().omitPadding().decode(base64)
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