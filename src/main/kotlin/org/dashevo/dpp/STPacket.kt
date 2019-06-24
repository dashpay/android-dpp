/**
 * Copyright (c) 2018-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashevo.dpp

import org.dashevo.dpp.util.HashUtils

abstract class STPacket(val contractId: String) {

    abstract fun getItemsMerkleRoot(): String

    abstract fun getItemsHash(): String

    abstract fun toJSON(): Map<String, Any>

    fun serialize(): ByteArray {
        return HashUtils.encode(this.toJSON())
    }

    fun hash(): ByteArray {
        return HashUtils.toHash(this.serialize())
    }

}