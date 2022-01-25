/**
 * Copyright (c) 2021-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashj.platform.dpp.errors

import org.dashj.platform.dpp.errors.concensus.Codes
import org.dashj.platform.dpp.toBase64
import org.dashj.platform.dpp.util.Cbor
import org.dashj.platform.dpp.util.Converters

/**
 * Concensus Errors are contained in the Metadata of trailers of StatusRuntimeExceptions
 *
 * Metadata(code=2002,drive-error-data-bin=oWlhcmd1bWVudHOA)
 */

class ConcensusErrorMetadata(metadata: String) : ErrorMetadata(metadata) {

    companion object {
        private const val metadataHeader = "Metadata("
        private const val codeField = "code="
        private const val dataField = "drive-error-data-bin="
        private const val codeFieldSize = codeField.length
        private const val dataFieldSize = dataField.length
    }
    val code: Codes
    val data: Map<String, Any?>
    val arguments: List<Any>
        get() {
            return if (data.containsKey("arguments")) {
                if (data["arguments"] is List<*>) {
                    data["arguments"] as List<Any>
                } else {
                    throw IllegalArgumentException("the Metadata for this error is invalid: $metadata")
                }
            } else {
                listOf()
            }
        }

    init {
        var cursor = 0
        if (metadata.startsWith(metadataHeader)) {
            cursor += metadataHeader.length
            val codeStart = metadata.indexOf(codeField, cursor) + codeFieldSize
            var codeEnd = metadata.indexOf(',', codeStart)

            if (codeEnd < 0) {
                codeEnd = metadata.indexOf(')', codeStart)
            }

            val codeString = metadata.substring(codeStart, codeEnd)
            code = Codes.getByCodeNoException(codeString.toInt())

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
            code = Codes.UnknownError
            data = mapOf()
        }
    }

    override fun toString(): String {
        return "Metadata(code=${code.code}, drive-error-data-bin=${Cbor.encode(data).toBase64()})\n" +
            "  arguments=$arguments"
    }
}
