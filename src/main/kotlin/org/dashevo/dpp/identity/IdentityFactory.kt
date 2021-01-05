/**
 * Copyright (c) 2018-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashevo.dpp.identity

import org.bitcoinj.core.TransactionOutPoint
import org.dashevo.dpp.Factory
import org.dashevo.dpp.StateRepository
import org.dashevo.dpp.identifier.Identifier
import org.dashevo.dpp.util.Cbor
import org.dashevo.dpp.util.CreditsConverter
import org.dashevo.dpp.util.HashUtils

class IdentityFactory(stateRepository: StateRepository) : Factory(stateRepository) {

    fun create(lockedOutPoint: TransactionOutPoint, publicKeys: List<IdentityPublicKey>, revision: Int, protocolVersion: Int) : Identity {
        val id = Identifier.from(lockedOutPoint.hash)
        return Identity(id, publicKeys, revision, protocolVersion)
    }

    fun create(id: Identifier, publicKeys: List<IdentityPublicKey>, revision: Int, protocolVersion: Int) : Identity {
        return Identity(id, publicKeys, revision, protocolVersion)
    }

    fun create(id: String, publicKeys: List<IdentityPublicKey>, revision: Int, protocolVersion: Int) : Identity {
        return Identity(Identifier.from(id), publicKeys, revision, protocolVersion)
    }

    fun createFromObject(rawIdentity: MutableMap<String, Any?>, options: Options = Options()): Identity {
        return Identity(rawIdentity)
    }

    fun createFromBuffer(payload: ByteArray, options: Options = Options()): Identity {
        val rawIdentity = Cbor.decode(payload).toMutableMap()
        return createFromObject(rawIdentity, options)
    }

    fun createIdentityCreateTransition(identity: Identity): IdentityCreateTransition {
        return  IdentityCreateTransition(identity.assetLock!!, identity.publicKeys, Identity.PROTOCOL_VERSION)
    }

    fun createIdentityCreateTransition(assetLock: AssetLock, identityPublicKeys: List<IdentityPublicKey>): IdentityCreateTransition {

        return  IdentityCreateTransition(assetLock, identityPublicKeys)
    }

    fun createIdentityTopupTransition(identityId: Identifier, assetLock: AssetLock): IdentityTopupTransition {
        return  IdentityTopupTransition(identityId, assetLock)
    }

    fun applyIdentityCreateStateTransition(stateTransition: IdentityStateTransition) : Identity {

        val identityCreateTransition = stateTransition as IdentityCreateTransition

        val output = identityCreateTransition.assetLock.output
        val outpoint = identityCreateTransition.assetLock.getOutPoint()

        val creditsAmount = CreditsConverter.convertSatoshiToCredits(output.value)

        val newIdentity = Identity(identityCreateTransition.identityId,
                0,
                identityCreateTransition.publicKeys,
                0,
                Identity.PROTOCOL_VERSION)

        val publicKeyHashes = newIdentity.publicKeys.map { HashUtils.toHash(it.data)}

        //store identity
        stateRepository.storeIdentity(newIdentity)
        stateRepository.storeIdentityPublicKeyHashes(newIdentity.id, publicKeyHashes)
        stateRepository.storeAssetLockTransactionOutPoint(outpoint)

        return newIdentity

    }
}