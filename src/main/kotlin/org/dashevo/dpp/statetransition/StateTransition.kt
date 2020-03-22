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
import org.dashevo.dpp.util.HashUtils
import java.lang.Exception

abstract class StateTransition(var signaturePublicKeyId: Int?,
                               var signature: String?, var type: Types, var protocolVersion: Int = 0) : BaseObject() {

    enum class Types(val value: Int) {
        DATA_CONTRACT(1),
        DOCUMENTS(2),
        IDENTITY_CREATE(3),
        IDENTITY_TOPUP(4),
        IDENTITY_UPDATEKEY(5),
        IDENTITY_CLOSEACCOUNT(6);
    }

    constructor(type: Types, protocolVersion: Int = 0) : this(null, null, type, protocolVersion)

    override fun toJSON(): Map<String, Any?> {
        return toJSON(false)
    }

    open fun toJSON(skipSignature: Boolean): Map<String, Any?> {
        val json = hashMapOf<String, Any?>()
        json["protocolVersion"] = protocolVersion
        json["type"] = type.value
        if(!skipSignature) {
            json["signature"] = signature
            json["signaturePublicKeyId"] = signaturePublicKeyId
        }
        return json
    }

    fun serialize(skipSignature: Boolean): ByteArray {
        return HashUtils.encode(this.toJSON(skipSignature))
    }

    fun sign(identityPublicKey: IdentityPublicKey, privateKey: String) {
        val data = serialize(true)
        val hash = HashUtils.toSha256Hash(data)
        var privateKeyModel : ECKey
        val pubKeyBase: String
        when (identityPublicKey.type) {
            IdentityPublicKey.TYPES.ECDSA_SECP256K1 -> {
                try {
                    val dpk = DumpedPrivateKey.fromBase58(EvoNetParams.get(), privateKey)
                    privateKeyModel = dpk.key
                } catch (_ : AddressFormatException) {
                    privateKeyModel = ECKey.fromPrivate(Utils.HEX.decode(privateKey))
                }
                pubKeyBase = privateKeyModel.pubKey.toBase64()
                if (pubKeyBase != identityPublicKey.data) {
                    throw InvalidSignaturePublicKeyError(identityPublicKey.data)
                }
                //does the data also include "\x18DarkCoin Message"?
                signature = HashSigner.signHash(hash, privateKeyModel).toStringBase64()
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

        val signatureBuffer = HashUtils.fromBase64(signature!!);

        val data = serialize(true)
        val hash = HashUtils.toSha256Hash(data)

        val publicKeyBuffer = HashUtils.fromBase64(publicKey.data)
        val publicKeyModel = ECKey.fromPublicOnly(publicKeyBuffer)
        val publicKeyId = publicKeyModel.pubKeyHash

        val sb = StringBuilder()
        return try {
            HashSigner.verifyHash(hash, publicKeyId, MasternodeSignature(EvoNetParams.get(), signatureBuffer, 0), sb)
        } catch (e : Exception) {
            System.out.println(sb.toString() + "\n" + e)
            false
        }
    }
}