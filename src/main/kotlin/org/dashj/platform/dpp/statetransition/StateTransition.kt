/**
 * Copyright (c) 2018-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashj.platform.dpp.statetransition

import org.bitcoinj.core.ECKey
import org.bitcoinj.core.HashSigner
import org.bitcoinj.core.MasternodeSignature
import org.bitcoinj.core.Sha256Hash
import org.bitcoinj.core.SignatureDecodeException
import org.bitcoinj.crypto.BLSPublicKey
import org.bitcoinj.crypto.BLSSecretKey
import org.bitcoinj.crypto.BLSSignature
import org.dashj.platform.dpp.BaseObject
import org.dashj.platform.dpp.ProtocolVersion
import org.dashj.platform.dpp.errors.concensus.signature.InvalidIdentityPublicKeyTypeException
import org.dashj.platform.dpp.hashTwice
import org.dashj.platform.dpp.identifier.Identifier
import org.dashj.platform.dpp.identity.IdentityPublicKey
import org.dashj.platform.dpp.statetransition.errors.StateTransitionIsNotSignedException
import org.dashj.platform.dpp.toBase64Padded
import org.dashj.platform.dpp.toSha256Hash
import org.dashj.platform.dpp.util.Converters
import java.security.SignatureException

abstract class StateTransition(
    var signature: ByteArray?,
    var type: Types,
    protocolVersion: Int = ProtocolVersion.latestVersion
) :
    BaseObject(protocolVersion) {

    enum class Types(val value: Int) {
        DATA_CONTRACT_CREATE(0),
        DOCUMENTS_BATCH(1),
        IDENTITY_CREATE(2),
        IDENTITY_TOP_UP(3),
        DATA_CONTRACT_UPDATE(4),
        IDENTITY_UPDATE(5);

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

    fun signByPrivateKey(privateKey: String, keyType: IdentityPublicKey.Type) {
        signByPrivateKey(Converters.byteArrayFromString(privateKey), keyType)
    }
    fun signByPrivateKey(privateKey: ByteArray, keyType: IdentityPublicKey.Type) {
        val data = hash(skipSignature = true)

        when (keyType) {
            IdentityPublicKey.Type.ECDSA_SECP256K1,
            IdentityPublicKey.Type.ECDSA_HASH160 -> {
                val privateKeyModel = ECKey.fromPrivate(privateKey)
                signature = privateKeyModel.signHash(Sha256Hash.wrap(data))
            }
            IdentityPublicKey.Type.BLS12_381 -> {
                val privateKeyModel = BLSSecretKey(privateKey)
                val blsSignature = privateKeyModel.Sign(Sha256Hash.wrap(data))

                signature = blsSignature.bitcoinSerialize()
            }
            else -> throw InvalidIdentityPublicKeyTypeException(keyType)
        }
    }

    fun signByPrivateKey(privateKey: ECKey) {
        val data = toBuffer(true)
        val hash = data.toSha256Hash()

        signature = privateKey.signHash(hash)
    }

    fun verifyByPublicKey(publicKey: ByteArray, publicKeyType: IdentityPublicKey.Type): Boolean {
        return when (publicKeyType) {
            IdentityPublicKey.Type.ECDSA_SECP256K1 -> verifyECDSASignatureByPublicKey(publicKey)
            IdentityPublicKey.Type.ECDSA_HASH160 -> verifyESDSAHash160SignatureByPublicKeyHash(publicKey)
            IdentityPublicKey.Type.BLS12_381 -> verifyBLSSignatureByPublicKey(publicKey)
            else -> throw InvalidIdentityPublicKeyTypeException(publicKeyType)
        }
    }

    fun verifyESDSAHash160SignatureByPublicKeyHash(publicKeyHash: ByteArray): Boolean {
        if (signature == null) {
            throw StateTransitionIsNotSignedException(this)
        }
        val hash = hash(skipSignature = true)
        return try {
            val errorMessage = StringBuilder()
            HashSigner.verifyHash(Sha256Hash.wrap(hash), publicKeyHash, MasternodeSignature(signature), errorMessage)
        } catch (e: Exception) {
            false
        }
    }

    fun verifyECDSASignatureByPublicKey(publicKey: ByteArray): Boolean {
        if (signature == null) {
            throw StateTransitionIsNotSignedException(this)
        }
        val hash = hash(skipSignature = true)
        val publicKeyModel = ECKey.fromPublicOnly(publicKey)
        return try {
            publicKeyModel.verify(hash, signature)
        } catch (e: SignatureDecodeException) {
            false
        }
    }
    /**
     * Verify signature with public key
     */
    fun verifyBLSSignatureByPublicKey(publicKey: ByteArray): Boolean {

        if (signature == null) {
            throw StateTransitionIsNotSignedException(this)
        }
        val hash = hash(skipSignature = true)
        val publicKeyModel = BLSPublicKey(publicKey)
        val blsSignature = BLSSignature(signature)
        return blsSignature.verifyInsecure(publicKeyModel, Sha256Hash.wrap(hash))
    }
    fun verifySignatureByPublicKey(publicKey: ECKey): Boolean {
        if (signature == null) {
            throw StateTransitionIsNotSignedException(this)
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
            throw StateTransitionIsNotSignedException(this)
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
