/**
 * Copyright (c) 2021-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashj.platform.dpp.errors.concensus

import org.dashj.platform.dpp.errors.ConcensusErrorMetadata
import org.dashj.platform.dpp.errors.DPPException
import org.dashj.platform.dpp.toBase64
import org.dashj.platform.dpp.util.Cbor

abstract class ConcensusException(message: String) : DPPException(message) {

    companion object {
        @JvmStatic
        fun create(code: Int, arguments: List<Any>): ConcensusException {
            try {
                val codeEnum = Codes.getByCode(code)
                return createException(codeEnum, arguments)
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("Error is not defined: $code", e)
            }
        }

        @JvmStatic
        fun create(code: Codes, arguments: List<Any>): ConcensusException {
            try {
                return createException(code, arguments)
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("Error is not defined: $code", e)
            }
        }

        @JvmStatic
        fun create(code: Codes, driveErrorData: Map<String, Any>): ConcensusException {
            try {
                return createException(code, driveErrorData)
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("Error is not defined: $code", e)
            }
        }

        @JvmStatic
        fun create(errorMetadata: ConcensusErrorMetadata): ConcensusException {
            return create(errorMetadata.code, errorMetadata.arguments)
        }

        private fun createException(code: Codes, driveErrorData: Map<String, Any>): ConcensusException {
            return createException(code, driveErrorData["arguments"] as List<Any>)
        }

        private fun createException(code: Codes, arguments: List<Any>): ConcensusException {
            return object : ConcensusException("error $code, $arguments") {}
        }
    }

    val arguments = arrayListOf<Any>()

    fun getCode(): Int {
        try {
            val code = Codes.getByName(name)
            return code.code
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Error is not defined: $name", e)
        }
    }

    fun setArguments(arguments: List<Any>) {
        this.arguments.addAll(arguments)
    }

    override fun toString(): String {
        val data = hashMapOf<String, Any?>(
            "arguments" to arguments
        )
        return "Metadata(code=${getCode()}, drive-error-data-bin=${Cbor.encode(data).toBase64()}"
    }
}
