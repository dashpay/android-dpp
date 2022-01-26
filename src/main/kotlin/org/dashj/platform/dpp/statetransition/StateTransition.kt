/**
 * Copyright (c) 2018-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashj.platform.dpp.statetransition

import org.bitcoinj.core.ECKey
import org.dashj.platform.dpp.BaseObject
import org.dashj.platform.dpp.ProtocolVersion
import org.dashj.platform.dpp.hashTwice
import org.dashj.platform.dpp.identifier.Identifier
import org.dashj.platform.dpp.statetransition.errors.StateTransitionIsNotSignedError
import org.dashj.platform.dpp.toBase64Padded
import org.dashj.platform.dpp.toSha256Hash
import org.dashj.platform.dpp.util.Converters
import java.security.SignatureException

abstract class StateTransition(
    var signature: ByteArray?,
    val type: Types,
    protocolVersion: Int = ProtocolVersion.latestVersion
) :
    BaseObject(protocolVersion) {

    enum class Types(val value: Int) {
        DATA_CONTRACT_CREATE(0),
        DOCUMENTS_BATCH(1),
        IDENTITY_CREATE(2),
        IDENTITY_TOP_UP(3),
        DATA_CONTRACT_UPDATE(4);

        companion object {
            private val values = values()
            fun getByCode(code: Int): Types {
                return values.filter { it.value == code }[0]
            }
        }
    }

    companion object {
        const val PRICE_PER_BYTE = 1L
    }

    abstract val modifiedDataIds: List<Identifier>
    var isSignatureVerified = false

    constructor(rawStateTransition: MutableMap<String, Any?>) :
        this(
            rawStateTransition["signature"]?.let { Converters.byteArrayFromBase64orByteArray(it) },
            Types.getByCode(rawStateTransition["type"] as Int),
            rawStateTransition["protocolVersion"] as Int
        )

    constructor(type: Types, protocolVersion: Int = ProtocolVersion.latestVersion) : this(null, type, protocolVersion)

    fun setSignature(signature: String): StateTransition {
        return setSignature(Converters.fromBase64(signature))
    }

    fun setSignature(signature: ByteArray): StateTransition {
        this.signature = signature
        return this
    }

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
        if (!skipSignature) {
            signature?.let { json["signature"] = it.toBase64Padded() }
        }
        return json
    }

    fun hash(skipSignature: Boolean): ByteArray {
        return toBuffer(skipSignature).hashTwice()
    }

    fun toBuffer(skipSignature: Boolean): ByteArray {
        val serializedData = toObject(skipSignature, false)
        serializedData.remove("protocolVersion")
        return encodeProtocolEntity(serializedData)
    }

    fun signByPrivateKey(privateKey: String) {
        signByPrivateKey(ECKey.fromPrivate(Converters.fromHex(privateKey)))
    }

    fun signByPrivateKey(privateKey: ECKey) {
        val data = toBuffer(true)
        val hash = data.toSha256Hash()

        signature = privateKey.signHash(hash)
    }

    fun verifySignatureByPublicKey(publicKey: ECKey): Boolean {
        if (signature == null) {
            throw StateTransitionIsNotSignedError(this)
        }

        val data = toBuffer(true)
        val hash = data.toSha256Hash()

        isSignatureVerified = try {
            val pubkeyFromSig = ECKey.signedMessageToKey(hash, signature)
            pubkeyFromSig.pubKey!!.contentEquals(publicKey.pubKey)
        } catch (e: SignatureException) {
            false
        }
        return isSignatureVerified
    }

    fun verifySignatureByPublicKeyHash(publicKeyHash: ByteArray): Boolean {

        if (signature == null) {
            throw StateTransitionIsNotSignedError(this)
        }

        val hash = this.toBuffer(true).toSha256Hash()

        isSignatureVerified = try {
            val pubkeyFromSig = ECKey.signedMessageToKey(hash, signature)
            pubkeyFromSig.pubKeyHash!!.contentEquals(publicKeyHash)
        } catch (e: SignatureException) {
            false
        }
        return isSignatureVerified
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
