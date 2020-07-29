/**
 * Copyright (c) 2018-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashevo.dpp.statetransition

import org.bitcoinj.core.*
import org.dashevo.dpp.BaseObject
import org.dashevo.dpp.statetransition.errors.StateTransitionIsNotSignedError
import org.dashevo.dpp.toBase64Padded
import org.dashevo.dpp.util.Cbor
import org.dashevo.dpp.util.HashUtils
import java.lang.Exception

abstract class StateTransition(var signature: String?,
                               val type: Types,
                               var protocolVersion: Int = CURRENT_PROTOCOL_VERSION)
    : BaseObject() {

    enum class Types(val value: Int) {
        DATA_CONTRACT_CREATE(0),
        DOCUMENTS_BATCH(1),
        IDENTITY_CREATE(2),
        IDENTITY_TOP_UP(3),
        IDENTITY_UPDATEKEY(4),
        IDENTITY_CLOSEACCOUNT(5);

        companion object {
            private val values = values()
            fun getByCode(code: Int): Types {
                return values.filter { it.value == code }[0]
            }
        }
    }

    companion object {
        const val PRICE_PER_BYTE = 1L
        const val CURRENT_PROTOCOL_VERSION = 0
    }

    constructor(rawStateTransition: MutableMap<String, Any?>) :
            this(rawStateTransition["signature"] as? String,
                    Types.getByCode(rawStateTransition["type"] as Int),
                    rawStateTransition["protocolVersion"] as Int)


    constructor(type: Types, protocolVersion: Int = 0) : this(null, type, protocolVersion)

    override fun toJSON(): MutableMap<String, Any?> {
        return toJSON(false)
    }

    open fun toJSON(skipSignature: Boolean): MutableMap<String, Any?> {
        val json = hashMapOf<String, Any?>()
        json["protocolVersion"] = protocolVersion
        json["type"] = type.value
        if (!skipSignature) {
            json["signature"] = signature
        }
        return json
    }

    fun serialize(skipSignature: Boolean): ByteArray {
        return Cbor.encode(this.toJSON(skipSignature))
    }

    fun signByPrivateKey(privateKey: ECKey) {
        val data = serialize(true)
        val hash = HashUtils.toSha256Hash(data)

        signature = privateKey.signHash(hash).toBase64Padded()
    }

    fun verifySignatureByPublicKey(publicKey: ECKey): Boolean {
        if (signature == null) {
            throw StateTransitionIsNotSignedError(this)
        }

        val signatureBuffer = HashUtils.fromBase64(signature!!)

        val data = serialize(true)
        val hash = HashUtils.toSha256Hash(data)

        return try {
            val pubkeyFromSig = ECKey.signedMessageToKey(hash, signatureBuffer)
            pubkeyFromSig.pubKey.contentEquals(publicKey.pubKey)
        } catch (e: Exception) {
            false
        }
    }

    fun calculateFee(): Long {
        val serializedStateTransition = serialize(skipSignature = true)
        return serializedStateTransition.size * PRICE_PER_BYTE
    }
}