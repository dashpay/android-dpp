/**
 * Copyright (c) 2020-present, Dash Core Group Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashevo.dpp

import org.dashevo.dpp.util.Cbor
import org.dashevo.dpp.util.HashUtils

/**
 * The abstract base class for Dash Platform objects that will handle
 * serialization and hashing of the object data
 */

abstract class BaseObject {

    /**
     * Return plain JSON object
     *
     */
    abstract fun toJSON(): Map<String, Any?>

    /**
     * Return serialized object
     *
     */
    fun serialize(): ByteArray {
        return Cbor.encode(this.toJSON())
    }

    /**
     * Return the double SHA256 hash of the serialized object
     *
     */
    fun hash(): String {
        return hashAsByteArray().toHexString()
    }

    open fun hashAsByteArray(): ByteArray {
        return HashUtils.toHash(this.serialize())
    }
}