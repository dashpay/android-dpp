/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashj.platform.dpp

import org.bitcoinj.core.Utils
import org.dashj.platform.dpp.util.Cbor

open class Factory(val dpp: DashPlatformProtocol, val stateRepository: StateRepository) {

    data class Options(val skipValidation: Boolean = false)

    /**
     * returns the protocol version and the Map of the buffer
     */
    companion object {
        @JvmStatic
        fun decodeProtocolEntity(buffer: ByteArray, protocolVersion: Int): Pair<Int, MutableMap<String, Any?>> {
            val protocolVersion = Utils.readUint32(buffer, 0).toInt()
            val rawEntity = buffer.copyOfRange(4, buffer.size)
            val rawObject = Cbor.decode(rawEntity)
            return Pair(protocolVersion, rawObject.toMutableMap())
        }
    }
}
