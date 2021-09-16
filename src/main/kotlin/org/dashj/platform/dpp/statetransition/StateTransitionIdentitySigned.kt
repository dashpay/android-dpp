/**
 * Copyright (c) 2018-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashj.platform.dpp.statetransition

import org.bitcoinj.core.AddressFormatException
import org.bitcoinj.core.DumpedPrivateKey
import org.bitcoinj.core.ECKey
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.params.TestNet3Params
import org.dashj.platform.dpp.ProtocolVersion
import org.dashj.platform.dpp.identity.IdentityPublicKey
import org.dashj.platform.dpp.statetransition.errors.InvalidSignaturePublicKeyError
import org.dashj.platform.dpp.statetransition.errors.InvalidSignatureTypeError
import org.dashj.platform.dpp.statetransition.errors.PublicKeyMismatchError
import org.dashj.platform.dpp.statetransition.errors.StateTransitionIsNotSignedError
import org.dashj.platform.dpp.toBase64
import org.dashj.platform.dpp.util.Converters

abstract class StateTransitionIdentitySigned(
    var signaturePublicKeyId: Int?,
    signature: ByteArray?,
    type: Types,
    protocolVersion: Int = 0
) :
    StateTransition(signature, type, protocolVersion) {

    constructor(rawStateTransition: MutableMap<String, Any?>) :
        this(
            rawStateTransition["signaturePublicKeyId"] as? Int,
            rawStateTransition["signature"]?.let { Converters.byteArrayFromBase64orByteArray(it) },
            Types.getByCode(rawStateTransition["type"] as Int),
            if (rawStateTransition.containsKey("protocolVersion")) {
                rawStateTransition["protocolVersion"] as Int
            } else ProtocolVersion.latestVersion
        )

    constructor(type: Types, protocolVersion: Int = ProtocolVersion.latestVersion) :
        this(null, null, type, protocolVersion)

    override fun toObject(skipSignature: Boolean, skipIdentifiersConversion: Boolean): MutableMap<String, Any?> {
        val rawStateTransition = super.toObject(skipSignature, skipIdentifiersConversion)
        if (!skipSignature) {
            rawStateTransition["signaturePublicKeyId"] = signaturePublicKeyId
        }
        return rawStateTransition
    }

    fun sign(identityPublicKey: IdentityPublicKey, privateKey: String) {
        val privateKeyModel: ECKey
        val pubKeyBase: ByteArray
        when (identityPublicKey.type) {
            IdentityPublicKey.TYPES.ECDSA_SECP256K1 -> {
                privateKeyModel = try {
                    DumpedPrivateKey.fromBase58(TestNet3Params.get(), privateKey).key
                } catch (_: AddressFormatException.WrongNetwork) {
                    DumpedPrivateKey.fromBase58(MainNetParams.get(), privateKey).key
                } catch (_: AddressFormatException) {
                    ECKey.fromPrivate(Converters.fromHex(privateKey))
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
