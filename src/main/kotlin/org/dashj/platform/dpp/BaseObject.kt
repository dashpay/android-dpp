/**
 * Copyright (c) 2020-present, Dash Core Group Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashj.platform.dpp

import org.bitcoinj.core.Sha256Hash
import org.bitcoinj.core.Utils
import org.dashj.platform.dpp.util.Cbor
import org.dashj.platform.dpp.util.HashUtils

/**
 * The abstract base class for Dash Platform objects that will handle
 * serialization and hashing of the object data
 */

abstract class BaseObject(var protocolVersion: Int = ProtocolVersion.latestVersion) {

    /**
     * Return plain JSON object
     *
     */
    abstract fun toJSON(): Map<String, Any?>

    /**
     * Return plain JSON object that encodes binary data as ByteArray
     *
     */
    abstract fun toObject(): Map<String, Any?>

    /**
     * Return serialized object
     *
     */
    fun toBuffer(): ByteArray {
        return encodeProtocolEntity(toObject())
    }

    fun encodeProtocolEntity(payload: Map<String, Any?>): ByteArray {
        val encoded = Cbor.encode(payload)
        val buffer = ByteArray(encoded.size + 4)
        Utils.uint32ToByteArrayLE(protocolVersion.toLong(), buffer, 0)
        System.arraycopy(encoded, 0, buffer, 4, encoded.size)
        return buffer
    }

    /**
     * Return the double SHA256 hash of the serialized object
     *
     */
    open fun hash(): ByteArray {
        return HashUtils.toHash(toBuffer())
    }

    open fun hashOnce(): ByteArray {
        return Sha256Hash.hash(toBuffer())
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BaseObject

        return hash().contentEquals(other.hash())
    }

    override fun hashCode(): Int {
        return Sha256Hash.wrap(hash()).hashCode()
    }
}
