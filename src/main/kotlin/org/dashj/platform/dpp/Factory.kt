/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashj.platform.dpp

import org.bitcoinj.core.VarInt
import org.dashj.platform.dpp.util.Cbor

open class Factory(val dpp: DashPlatformProtocol, val stateRepository: StateRepository) {

    data class Options(val skipValidation: Boolean = false)

    /**
     * returns the protocol version and the Map of the buffer
     */
    companion object {
        @JvmStatic
        fun decodeProtocolEntity(buffer: ByteArray): Pair<Int, MutableMap<String, Any?>> {
            val protocolVersionCompact = VarInt(buffer, 0)
            val protocolVersion = protocolVersionCompact.value
            val rawEntity = buffer.copyOfRange(protocolVersionCompact.sizeInBytes, buffer.size)
            val rawObject = Cbor.decode(rawEntity)
            return Pair(protocolVersion.toInt(), rawObject.toMutableMap())
        }
    }
}
