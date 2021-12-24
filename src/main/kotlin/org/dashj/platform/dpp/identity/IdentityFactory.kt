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
import org.dashj.platform.dpp.DashPlatformProtocol
import org.dashj.platform.dpp.Factory
import org.dashj.platform.dpp.ProtocolVersion
import org.dashj.platform.dpp.StateRepository
import org.dashj.platform.dpp.identifier.Identifier
import org.dashj.platform.dpp.statetransition.AssetLockProofFactory
import org.dashj.platform.dpp.util.CreditsConverter.convertSatoshiToCredits

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

    fun create(assetLockProof: AssetLockProof, publicKeys: List<ECKey>): Identity {
        return Identity(
            assetLockProof.createIdentifier(),
            publicKeys.mapIndexed {
                index, it ->
                IdentityPublicKey(
                    index,
                    IdentityPublicKey.TYPES.ECDSA_SECP256K1,
                    IdentityPublicKey.Purpose.AUTHENTICATION,
                    IdentityPublicKey.SecurityLevel.MASTER,
                    it.pubKey,
                    true
                )
            },
            revision = 0,
            protocolVersion = ProtocolVersion.latestVersion
        )
    }

    fun createFromObject(rawIdentity: MutableMap<String, Any?>, options: Options = Options()): Identity {
        return Identity(rawIdentity)
    }

    fun createFromBuffer(payload: ByteArray, options: Options = Options()): Identity {
        val (protocolVersion, rawIdentity) = decodeProtocolEntity(payload)
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

    fun createIdentityCreateTransition(identity: Identity): IdentityCreateTransition {
        return IdentityCreateTransition(identity.assetLockProof!!, identity.publicKeys, ProtocolVersion.latestVersion)
    }

    fun createIdentityCreateTransition(
        assetLock: AssetLockProof,
        identityPublicKeys: List<IdentityPublicKey>
    ): IdentityCreateTransition {
        return IdentityCreateTransition(assetLock, identityPublicKeys)
    }

    fun createIdentityTopupTransition(
        identityId: Identifier,
        assetLock: AssetLockProof
    ): IdentityTopUpTransition {
        return IdentityTopUpTransition(identityId, assetLock)
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
            identityCreateTransition.publicKeys,
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
}
