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

abstract class StateTransitionIdentitySigned(var signaturePublicKeyId: Int?,
                                             signature: String?,
                                             type: Types,
                                             protocolVersion: Int = 0)
    : StateTransition(signature, type, protocolVersion) {

    constructor(rawStateTransition: MutableMap<String, Any?>) :
            this(rawStateTransition["signaturePublicKeyId"] as? Int,
                    rawStateTransition["signature"] as? String,
                    Types.getByCode(rawStateTransition["type"] as Int),
                    if (rawStateTransition.containsKey("protocolVersion"))
                        rawStateTransition["protocolVersion"] as Int
                    else CURRENT_PROTOCOL_VERSION
            )

    constructor(type: Types, protocolVersion: Int = CURRENT_PROTOCOL_VERSION) : this(null, null, type, protocolVersion)

    override fun toJSON(skipSignature: Boolean): Map<String, Any?> {
        val json = super.toJSON(skipSignature) as HashMap<String, Any?>
        if (!skipSignature) {
            json["signaturePublicKeyId"] = signaturePublicKeyId
        }
        return json
    }


    fun sign(identityPublicKey: IdentityPublicKey, privateKey: String) {
        val data = serialize(true)
        val hash = HashUtils.toSha256Hash(data)
        var privateKeyModel: ECKey
        val pubKeyBase: String
        when (identityPublicKey.type) {
            IdentityPublicKey.TYPES.ECDSA_SECP256K1 -> {
                privateKeyModel = try {
                    val dpk = DumpedPrivateKey.fromBase58(EvoNetParams.get(), privateKey)
                    dpk.key
                } catch (_: AddressFormatException) {
                    ECKey.fromPrivate(HashUtils.fromHex(privateKey))
                }
                pubKeyBase = privateKeyModel.pubKey.toBase64()
                if (pubKeyBase != identityPublicKey.data) {
                    throw InvalidSignaturePublicKeyError(identityPublicKey.data)
                }
                signByPrivateKey(privateKeyModel)
            }
            else -> {
                throw InvalidSignatureTypeError(identityPublicKey.type)
            }
        }
        signaturePublicKeyId = identityPublicKey.id
    }


    fun verifySignature(publicKey: IdentityPublicKey): Boolean {
        if (signature == null) {
            throw StateTransitionIsNotSignedError(this);
        }

        if (signaturePublicKeyId != publicKey.id) {
            throw PublicKeyMismatchError(publicKey);
        }

        val publicKeyBuffer = HashUtils.fromBase64(publicKey.data)
        val publicKeyModel = ECKey.fromPublicOnly(publicKeyBuffer)

        return verifySignatureByPublicKey(publicKeyModel)
    }
}