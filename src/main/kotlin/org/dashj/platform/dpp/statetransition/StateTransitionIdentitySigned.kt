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
import org.bitcoinj.core.NetworkParameters
import org.dashj.platform.dpp.ProtocolVersion
import org.dashj.platform.dpp.identity.IdentityPublicKey
import org.dashj.platform.dpp.statetransition.errors.InvalidSignaturePublicKeyException
import org.dashj.platform.dpp.statetransition.errors.InvalidSignatureTypeException
import org.dashj.platform.dpp.statetransition.errors.PublicKeyMismatchError
import org.dashj.platform.dpp.statetransition.errors.PublicKeySecurityLevelNotMetException
import org.dashj.platform.dpp.statetransition.errors.StateTransitionIsNotSignedError
import org.dashj.platform.dpp.statetransition.errors.WrongPublicKeyPurposeException
import org.dashj.platform.dpp.toBase64
import org.dashj.platform.dpp.util.Converters

abstract class StateTransitionIdentitySigned(
    val params: NetworkParameters,
    var signaturePublicKeyId: Int?,
    signature: ByteArray?,
    type: Types,
    protocolVersion: Int = ProtocolVersion.latestVersion
) :
    StateTransition(signature, type, protocolVersion) {

    constructor(params: NetworkParameters, rawStateTransition: Map<String, Any?>) :
        this(
            params,
            rawStateTransition["signaturePublicKeyId"] as? Int,
            rawStateTransition["signature"]?.let { Converters.byteArrayFromBase64orByteArray(it) },
            Types.getByCode(rawStateTransition["type"] as Int),
            if (rawStateTransition.containsKey("protocolVersion")) {
                rawStateTransition["protocolVersion"] as Int
            } else ProtocolVersion.latestVersion
        )

    constructor(params: NetworkParameters, type: Types, protocolVersion: Int = ProtocolVersion.latestVersion) :
        this(params, null, null, type, protocolVersion)

    override fun toObject(skipSignature: Boolean, skipIdentifiersConversion: Boolean): MutableMap<String, Any?> {
        val rawStateTransition = super.toObject(skipSignature, skipIdentifiersConversion)
        if (!skipSignature) {
            rawStateTransition["signaturePublicKeyId"] = signaturePublicKeyId
        }
        return rawStateTransition
    }

    fun sign(identityPublicKey: IdentityPublicKey, privateKey: String) {
        sign(identityPublicKey, getPrivateECKey(privateKey, identityPublicKey))
    }

    fun sign(identityPublicKey: IdentityPublicKey, privateKey: ECKey) {
        sign(identityPublicKey, privateKey.privKeyBytes)
    }

    fun sign(identityPublicKey: IdentityPublicKey, privateKey: ByteArray) {
        val privateKeyModel: ECKey
        val pubKeyBase: ByteArray

        verifyPublicKeyLevelAndPurpose(identityPublicKey)

        when (identityPublicKey.type) {
            IdentityPublicKey.TYPES.ECDSA_SECP256K1 -> {
                privateKeyModel = ECKey.fromPrivate(privateKey)
                pubKeyBase = privateKeyModel.pubKey
                if (!pubKeyBase.contentEquals(identityPublicKey.data)) {
                    throw InvalidSignaturePublicKeyException(identityPublicKey.data.toBase64())
                }
                signByPrivateKey(privateKeyModel)
            }
            IdentityPublicKey.TYPES.ECDSA_HASH160 -> {
                privateKeyModel = ECKey.fromPrivate(privateKey)
                pubKeyBase = privateKeyModel.pubKeyHash
                if (!pubKeyBase.contentEquals(identityPublicKey.data)) {
                    throw InvalidSignaturePublicKeyException(identityPublicKey.data.toBase64())
                }
                signByPrivateKey(privateKeyModel)
            }
            IdentityPublicKey.TYPES.BLS12_381 -> {
                throw InvalidSignatureTypeException(identityPublicKey.type)
            }
            else -> {
                throw InvalidSignatureTypeException(identityPublicKey.type)
            }
        }
        signaturePublicKeyId = identityPublicKey.id
    }

    /**
     * returns a ECKey for the private key associated with privateKey
     * or throws an exception
     */
    private fun getPrivateECKey(
        privateKey: String,
        identityPublicKey: IdentityPublicKey
    ) = try {
        DumpedPrivateKey.fromBase58(params, privateKey).key
    } catch (_: AddressFormatException.WrongNetwork) {
        // the WIF is on the wrong network
        throw InvalidSignaturePublicKeyException(identityPublicKey.data.toBase64())
    } catch (_: AddressFormatException) {
        ECKey.fromPrivate(Converters.fromHex(privateKey))
    }

    fun verifySignature(publicKey: IdentityPublicKey): Boolean {
        verifyPublicKeyLevelAndPurpose(publicKey)

        if (signature == null) {
            throw StateTransitionIsNotSignedError(this)
        }

        if (signaturePublicKeyId != publicKey.id) {
            throw PublicKeyMismatchError(publicKey)
        }

        if (publicKey.type == IdentityPublicKey.TYPES.ECDSA_HASH160) {
            return verifySignatureByPublicKeyHash(publicKey.data)
        }

        val publicKeyModel = ECKey.fromPublicOnly(publicKey.data)

        return verifySignatureByPublicKey(publicKeyModel)
    }

    /**
     *
     * Verifies that the supplied public key has the correct security level
     * and purpose to sign this state transition
     */

    private fun verifyPublicKeyLevelAndPurpose(publicKey: IdentityPublicKey) {
        // If state transition requires MASTER security level it must be sign only with MASTER key
        if (publicKey.isMaster() && getKeySecurityLevelRequirement() != IdentityPublicKey.SecurityLevel.MASTER) {
            throw InvalidSignaturePublicKeySecurityLevelException(
                IdentityPublicKey.SecurityLevel.MASTER,
                this.getKeySecurityLevelRequirement(),
            )
        }

        // Otherwise, key security level should be less than MASTER but more or equal than required
        if (getKeySecurityLevelRequirement() < publicKey.securityLevel) {
            throw PublicKeySecurityLevelNotMetException(
                publicKey.securityLevel,
                this.getKeySecurityLevelRequirement(),
            )
        }

        if (publicKey.purpose != IdentityPublicKey.Purpose.AUTHENTICATION) {
            throw WrongPublicKeyPurposeException(
                publicKey.purpose,
                IdentityPublicKey.Purpose.AUTHENTICATION,
            )
        }
    }

    /**
     * Returns minimal key security level that can be used to sign this ST.
     * Override this method if the ST requires a different security level.
     *
     * @return {number}
     */
    open fun getKeySecurityLevelRequirement(): IdentityPublicKey.SecurityLevel {
        return IdentityPublicKey.SecurityLevel.HIGH
    }
}
