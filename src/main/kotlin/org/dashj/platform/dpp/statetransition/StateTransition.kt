/**
 * Copyright (c) 2018-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashj.platform.dpp.statetransition

import org.bitcoinj.core.*
import org.dashj.platform.dpp.BaseObject
import org.dashj.platform.dpp.identifier.Identifier
import org.dashj.platform.dpp.statetransition.errors.StateTransitionIsNotSignedError
import org.dashj.platform.dpp.toBase64Padded
import org.dashj.platform.dpp.util.Cbor
import org.dashj.platform.dpp.util.HashUtils
import java.lang.Exception

abstract class StateTransition(var signature: ByteArray?,
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

    abstract val modifiedDataIds: List<Identifier>

    constructor(rawStateTransition: MutableMap<String, Any?>) :
            this(rawStateTransition["signature"]?.let { HashUtils.byteArrayfromBase64orByteArray(it) },
                    Types.getByCode(rawStateTransition["type"] as Int),
                    rawStateTransition["protocolVersion"] as Int)


    constructor(type: Types, protocolVersion: Int = 0) : this(null, type, protocolVersion)

    override fun toObject(): MutableMap<String, Any?> {
        return toObject(skipSignature = false, skipIdentifiersConversion = false)
    }

    open fun toObject(skipSignature: Boolean, skipIdentifiersConversion: Boolean): MutableMap<String, Any?> {
        val json = hashMapOf<String, Any?>()
        json["protocolVersion"] = protocolVersion
        json["type"] = type.value
        if (!skipSignature) {
            json["signature"] = signature
        }
        return json
    }

    override fun toJSON(): MutableMap<String, Any?> {
        return toJSON(false)
    }

    open fun toJSON(skipSignature: Boolean): MutableMap<String, Any?> {
        val json = toObject(skipSignature, true)
        signature?.let { json["signature"] = it.toBase64Padded() }
        return json
    }

    fun toBuffer(skipSignature: Boolean): ByteArray {
        return Cbor.encode(toObject(skipSignature, false))
    }

    fun signByPrivateKey(privateKey: ECKey) {
        val data = toBuffer(true)
        val hash = HashUtils.toSha256Hash(data)

        signature = privateKey.signHash(hash)
    }

    fun verifySignatureByPublicKey(publicKey: ECKey): Boolean {
        if (signature == null) {
            throw StateTransitionIsNotSignedError(this)
        }

        val data = toBuffer(true)
        val hash = HashUtils.toSha256Hash(data)

        return try {
            val pubkeyFromSig = ECKey.signedMessageToKey(hash, signature)
            pubkeyFromSig.pubKey!!.contentEquals(publicKey.pubKey)
        } catch (e: Exception) {
            false
        }
    }

    fun calculateFee(): Long {
        val serializedStateTransition = toBuffer(skipSignature = true)
        return serializedStateTransition.size * PRICE_PER_BYTE
    }

    /** returns true of this state transition affects documents:  create, update and delete transitions */
    abstract fun isDocumentStateTransition(): Boolean
    /** returns true of this state transition affects data contracts */
    abstract fun isDataContractStateTransition(): Boolean
    /** returns true of this state transition affects identities: create, update and topup */
    abstract fun isIdentityStateTransition(): Boolean
}