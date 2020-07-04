/**
 * Copyright (c) 2018-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashevo.dpp.statetransition

import org.bitcoinj.core.*
import org.bitcoinj.params.EvoNetParams
import org.dashevo.dpp.BaseObject
import org.dashevo.dpp.identity.IdentityPublicKey
import org.dashevo.dpp.statetransition.errors.InvalidSignaturePublicKeyError
import org.dashevo.dpp.statetransition.errors.InvalidSignatureTypeError
import org.dashevo.dpp.statetransition.errors.PublicKeyMismatchError
import org.dashevo.dpp.statetransition.errors.StateTransitionIsNotSignedError
import org.dashevo.dpp.toBase64
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

    override fun toJSON(): Map<String, Any?> {
        return toJSON(false)
    }

    open fun toJSON(skipSignature: Boolean): Map<String, Any?> {
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

    /*fun sign(identityPublicKey: IdentityPublicKey, privateKey: String) {
        val data = serialize(true)
        val hash = HashUtils.toSha256Hash(data)
        var privateKeyModel: ECKey
        val pubKeyBase: String
        when (identityPublicKey.type) {
            IdentityPublicKey.TYPES.ECDSA_SECP256K1 -> {
                try {
                    val dpk = DumpedPrivateKey.fromBase58(EvoNetParams.get(), privateKey)
                    privateKeyModel = dpk.key
                } catch (_: AddressFormatException) {
                    privateKeyModel = ECKey.fromPrivate(HashUtils.fromHex(privateKey))
                }
                pubKeyBase = privateKeyModel.pubKey.toBase64()
                if (pubKeyBase != identityPublicKey.data) {
                    throw InvalidSignaturePublicKeyError(identityPublicKey.data)
                }
                signature = privateKeyModel.signHash(hash).toBase64Padded()
            }
            else -> {
                throw InvalidSignatureTypeError(identityPublicKey.type)
            }
        }
        signaturePublicKeyId = identityPublicKey.id
    }*/

    fun signByPrivateKey(privateKey: ECKey) {
        val data = serialize(true)
        val hash = HashUtils.toSha256Hash(data)

        signature = privateKey.signHash(hash).toBase64Padded()
    }

    /*fun verifySignature(publicKey: IdentityPublicKey): Boolean {
        if (signature == null) {
            throw StateTransitionIsNotSignedError(this);
        }

        if (signaturePublicKeyId != publicKey.id) {
            throw PublicKeyMismatchError(publicKey);
        }

        val signatureBuffer = HashUtils.fromBase64(signature!!);

        val data = serialize(true)
        val hash = HashUtils.toSha256Hash(data)

        val publicKeyBuffer = HashUtils.fromBase64(publicKey.data)
        val publicKeyModel = ECKey.fromPublicOnly(publicKeyBuffer)
        val publicKeyId = publicKeyModel.pubKeyHash

        val sb = StringBuilder()
        return try {
            val pubkeyFromSig = ECKey.signedMessageToKey(hash, signatureBuffer)
            pubkeyFromSig.pubKey.contentEquals(publicKeyBuffer)
        } catch (e: Exception) {
            false
        }
    }*/

    fun verifySignatureByPublicKey(publicKey: ECKey): Boolean {
        if (signature == null) {
            throw StateTransitionIsNotSignedError(this);
        }

        val signatureBuffer = HashUtils.fromBase64(signature!!);

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
        val serializedStateTransition = serialize(skipSignature = true);
        return serializedStateTransition.size * PRICE_PER_BYTE;
    }
}