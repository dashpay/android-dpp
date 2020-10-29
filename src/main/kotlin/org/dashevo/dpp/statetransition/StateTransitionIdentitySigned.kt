/**
 * Copyright (c) 2018-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashevo.dpp.statetransition

import org.bitcoinj.core.*
import org.bitcoinj.params.EvoNetParams
import org.dashevo.dpp.identity.IdentityPublicKey
import org.dashevo.dpp.statetransition.errors.InvalidSignaturePublicKeyError
import org.dashevo.dpp.statetransition.errors.InvalidSignatureTypeError
import org.dashevo.dpp.statetransition.errors.PublicKeyMismatchError
import org.dashevo.dpp.statetransition.errors.StateTransitionIsNotSignedError
import org.dashevo.dpp.toBase64
import org.dashevo.dpp.util.HashUtils

abstract class StateTransitionIdentitySigned(var signaturePublicKeyId: Int?,
                                             signature: ByteArray?,
                                             type: Types,
                                             protocolVersion: Int = 0)
    : StateTransition(signature, type, protocolVersion) {

    constructor(rawStateTransition: MutableMap<String, Any?>) :
            this(rawStateTransition["signaturePublicKeyId"] as? Int,
                    rawStateTransition["signature"]?.let { HashUtils.byteArrayfromBase64orByteArray(it) },
                    Types.getByCode(rawStateTransition["type"] as Int),
                    if (rawStateTransition.containsKey("protocolVersion"))
                        rawStateTransition["protocolVersion"] as Int
                    else CURRENT_PROTOCOL_VERSION
            )

    constructor(type: Types, protocolVersion: Int = CURRENT_PROTOCOL_VERSION) : this(null, null, type, protocolVersion)

    override fun toObject(skipSignature: Boolean, skipIdentifiersConversion: Boolean): MutableMap<String, Any?> {
        val rawStateTransition = super.toObject(skipSignature, skipIdentifiersConversion)
        if (!skipSignature) {
            rawStateTransition["signaturePublicKeyId"] = signaturePublicKeyId
        }
        return rawStateTransition
    }

    fun sign(identityPublicKey: IdentityPublicKey, privateKey: String) {
        var privateKeyModel: ECKey
        val pubKeyBase: ByteArray
        when (identityPublicKey.type) {
            IdentityPublicKey.TYPES.ECDSA_SECP256K1 -> {
                privateKeyModel = try {
                    val dpk = DumpedPrivateKey.fromBase58(EvoNetParams.get(), privateKey)
                    dpk.key
                } catch (_: AddressFormatException) {
                    ECKey.fromPrivate(HashUtils.fromHex(privateKey))
                }
                pubKeyBase = privateKeyModel.pubKey
                if (!pubKeyBase.contentEquals(identityPublicKey.data)) {
                    throw InvalidSignaturePublicKeyError(identityPublicKey.data.toBase64())
                }
                signByPrivateKey(privateKeyModel)
            }
            IdentityPublicKey.TYPES.BLS12_381 -> {
                throw InvalidSignatureTypeError(identityPublicKey.type)
            }
            else -> {
                throw InvalidSignatureTypeError(identityPublicKey.type)
            }
        }
        signaturePublicKeyId = identityPublicKey.id
    }


    fun verifySignature(publicKey: IdentityPublicKey): Boolean {
        if (signature == null) {
            throw StateTransitionIsNotSignedError(this)
        }

        if (signaturePublicKeyId != publicKey.id) {
            throw PublicKeyMismatchError(publicKey)
        }

        val publicKeyModel = ECKey.fromPublicOnly(publicKey.data)

        return verifySignatureByPublicKey(publicKeyModel)
    }
}