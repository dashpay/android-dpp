/**
 * Copyright (c) 2022-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashj.platform.dpp.errors

import org.dashj.platform.dpp.util.Cbor
import org.dashj.platform.dpp.util.Converters

/**
 * Drive Errors are contained in the Metadata of trailers of StatusRuntimeException
 *
 * Unlike with [ConcensusErrorMetadata], there is no error code.
 *
 * Metadata(drive-error-data-bin=oWZlcnJvcnOBoWRuYW1leCpOb3RJbmRleGVkUHJvcGVydGllc0luV2hlcmVDb25kaXRpb25zRXJyb3I)
*/

class DriveErrorMetadata(metadata: String) : ErrorMetadata(metadata) {

    companion object {
        private const val metadataHeader = "Metadata("
        private const val dataField = "drive-error-data-bin="
        private const val dataFieldSize = dataField.length
    }

    val data: Map<String, Any?>

    fun getFirstError(): String {
        return if (data.isNotEmpty()) {
            (data["errors"] as List<Map<String, Any?>>).first()["name"] as String
        } else {
            "No extra error information specified."
        }
    }

    init {
        var cursor = 0
        if (metadata.startsWith(metadataHeader)) {
            cursor += metadataHeader.length

            val dataStart = metadata.indexOf(dataField, cursor) + dataFieldSize
            data = if (dataStart != -1) {
                val dataEnd = metadata.indexOf(')', dataStart)
                if (dataEnd != -1) {
                    val dataString = metadata.substring(dataStart, dataEnd)
                    val dataBytes = Converters.fromBase64(dataString)
                    Cbor.decode(dataBytes)
                } else {
                    mapOf()
                }
            } else {
                mapOf()
            }
        } else {
            data = mapOf()
        }
    }

    override fun toString(): String {
        return if (data.isEmpty()) {
            super.metadata
        } else {
            "$metadataHeader$data)"
        }
    }
}
