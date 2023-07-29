/**
 * Copyright (c) 2018-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashj.platform.dpp.identity

import org.bitcoinj.core.ECKey
import org.bitcoinj.core.Transaction
import org.bitcoinj.core.TransactionOutPoint
import org.bitcoinj.quorums.InstantSendLock
import org.dashj.dpp.DPP
import org.dashj.platform.dpp.DashPlatformProtocol
import org.dashj.platform.dpp.Factory
import org.dashj.platform.dpp.ProtocolVersion
import org.dashj.platform.dpp.StateRepository
import org.dashj.platform.dpp.identifier.Identifier
import org.dashj.platform.dpp.statetransition.AssetLockProofFactory
import org.dashj.platform.dpp.util.CreditsConverter.convertSatoshiToCredits
import org.dashj.platform.dpp.validation.ValidationResult
import java.util.Date

class IdentityFactory(dpp: DashPlatformProtocol, stateRepository: StateRepository) : Factory(dpp, stateRepository) {

    fun create(
        lockedOutPoint: TransactionOutPoint,
        publicKeys: List<IdentityPublicKey>,
        revision: Int,
        protocolVersion: Int
    ): Identity {
        val id = Identifier.from(lockedOutPoint.hash)
        return Identity(id, publicKeys, revision, protocolVersion)
    }

    fun create(id: Identifier, publicKeys: List<IdentityPublicKey>, revision: Int, protocolVersion: Int): Identity {
        return Identity(id, publicKeys, revision, protocolVersion)
    }

    fun create(id: String, publicKeys: List<IdentityPublicKey>, revision: Int, protocolVersion: Int): Identity {
        return Identity(Identifier.from(id), publicKeys, revision, protocolVersion)
    }

    fun createFromKeyList(assetLockProof: AssetLockProof, publicKeys: List<ECKey>): Identity {
        return Identity(
            assetLockProof.createIdentifier(),
            publicKeys.mapIndexed {
                index, it ->
                IdentityPublicKey(
                    index,
                    IdentityPublicKey.Type.ECDSA_SECP256K1,
                    IdentityPublicKey.Purpose.AUTHENTICATION,
                    IdentityPublicKey.SecurityLevel.MASTER,
                    it.pubKey,
                    false
                )
            },
            revision = 0,
            protocolVersion = ProtocolVersion.latestVersion
        )
    }

    fun create(assetLockProof: AssetLockProof, publicKeyConfigs: List<Map<String, Any>>): Identity {
        val identity = Identity(
            assetLockProof.createIdentifier(),
            publicKeyConfigs.map { publicKey -> IdentityPublicKey(publicKey) },
            0,
            ProtocolVersion.latestVersion
        )

        identity.assetLockProof = assetLockProof

        return identity
    }

    fun createFromObject(rawIdentity: Map<String, Any?>, options: Options = Options()): Identity {
        return Identity(rawIdentity)
    }

    fun createFromBuffer(payload: ByteArray, options: Options = Options()): Identity {
        val payloadCbor = DPP.getIdentityCborFromBincode(payload)
        val (protocolVersion, rawIdentity) = decodeProtocolEntity(payloadCbor)
        rawIdentity["protocolVersion"] = protocolVersion
        return createFromObject(rawIdentity, options)
    }

    fun createInstantAssetLockProof(
        instantSendLock: InstantSendLock,
        assetLockTransaction: Transaction,
        outputIndex: Long
    ): InstantAssetLockProof {
        return InstantAssetLockProof(outputIndex, assetLockTransaction, instantSendLock)
    }

    fun createChainAssetLockProof(
        coreChainLockedHeight: Long,
        outPoint: TransactionOutPoint
    ): ChainAssetLockProof {
        return ChainAssetLockProof(coreChainLockedHeight, outPoint)
    }

    fun createChainAssetLockProof(
        coreChainLockedHeight: Long,
        outPoint: ByteArray
    ): ChainAssetLockProof {
        return ChainAssetLockProof(dpp.getNetworkParameters(), coreChainLockedHeight, outPoint)
    }

    fun createIdentityCreateTransition(identity: Identity): IdentityCreateTransition {
        return IdentityCreateTransition(
            dpp.getNetworkParameters(),
            identity.assetLockProof!!,
            identity.publicKeys,
            ProtocolVersion.latestVersion
        )
    }

    fun createIdentityCreateTransition(
        assetLock: AssetLockProof,
        identityPublicKeys: List<IdentityPublicKey>
    ): IdentityCreateTransition {
        return IdentityCreateTransition(dpp.getNetworkParameters(), assetLock, identityPublicKeys)
    }

    fun createIdentityTopUpTransition(
        identityId: Identifier,
        assetLock: AssetLockProof
    ): IdentityTopUpTransition {
        return IdentityTopUpTransition(dpp.getNetworkParameters(), identityId, assetLock)
    }

    fun createIdentityUpdateTransition(identity: Identity, rawPublicKeys: Map<String, Any>): IdentityUpdateTransition {
        return createIdentityUpdateTransition(
            identity,
            (rawPublicKeys["add"] as? List<Map<String, Any?>>)?.map { pk -> IdentityPublicKey(pk) },
            rawPublicKeys["disablePublicKeys"] as? List<IdentityPublicKey>
        )
    }
    fun createIdentityUpdateTransition(
        identity: Identity,
        publicKeysToAdd: List<IdentityPublicKey>?,
        publicKeysToDisable: List<IdentityPublicKey>?
    ): IdentityUpdateTransition {
        return IdentityUpdateTransition(
            dpp.params,
            identity.id,
            identity.revision + 1,
            publicKeysToAdd,
            publicKeysToDisable?.map { pk -> pk.id },
            if (publicKeysToDisable != null) Date().time else null
        )
    }

    fun applyIdentityCreateStateTransition(stateTransition: IdentityStateTransition): Identity {
        val identityCreateTransition = stateTransition as IdentityCreateTransition

        val output = AssetLockProofFactory(stateRepository).fetchAssetLockTransactionOutput(
            identityCreateTransition.assetLockProof
        )
        val outpoint = identityCreateTransition.assetLockProof.getOutPoint()

        val creditsAmount = convertSatoshiToCredits(output.value)

        val newIdentity = Identity(
            identityCreateTransition.identityId,
            0,
            identityCreateTransition.publicKeys.map { it.copy(skipSignature = true) }.toMutableList(),
            0,
            ProtocolVersion.latestVersion
        )

        val publicKeyHashes = newIdentity.publicKeys.map { ECKey.fromPublicOnly(it.data).pubKeyHash }

        // store identity
        stateRepository.storeIdentity(newIdentity)
        stateRepository.storeIdentityPublicKeyHashes(newIdentity.id, publicKeyHashes)
        stateRepository.markAssetLockTransactionOutPointAsUsed(outpoint)

        return newIdentity
    }

    fun applyIdentityTopUpTransition(stateTransition: IdentityTopUpTransition) {
        val output = AssetLockProofFactory(stateRepository).fetchAssetLockTransactionOutput(stateTransition.assetLock)
        val outPoint = stateTransition.assetLock.getOutPoint()

        val creditsAmount = convertSatoshiToCredits(output.value)
        val identityId = stateTransition.identityId

        val identity = stateRepository.fetchIdentity(identityId)
        identity!!.increaseBalance(creditsAmount)

        stateRepository.storeIdentity(identity)

        stateRepository.markAssetLockTransactionOutPointAsUsed(outPoint)
    }

    fun validate(identity: Identity): ValidationResult {
        return validate(identity.toObject())
    }

    fun validate(rawIdentity: Map<String, Any?>): ValidationResult {
        return ValidationResult(listOf())
    }
}
