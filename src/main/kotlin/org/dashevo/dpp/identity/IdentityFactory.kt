/**
 * Copyright (c) 2018-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashevo.dpp.identity

import org.bitcoinj.core.TransactionOutPoint
import org.dashevo.dpp.Factory
import org.dashevo.dpp.identifier.Identifier
import org.dashevo.dpp.util.Cbor

class IdentityFactory : Factory() {

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

    fun createFromSerialized(payload: ByteArray, options: Options = Options()): Identity {
        val rawIdentity = Cbor.decode(payload).toMutableMap()
        return createFromObject(rawIdentity, options)
    }

    fun createIdentityCreateTransition(identity: Identity): IdentityCreateTransition {
        val lockedOutpoint = identity.lockedOutpoint!!.bitcoinSerialize()

        return  IdentityCreateTransition(lockedOutpoint, identity.publicKeys)
    }

    fun createIdentityCreateTransition(lockedOutpoint: ByteArray, identityPublicKeys: List<IdentityPublicKey>): IdentityCreateTransition {

        return  IdentityCreateTransition(lockedOutpoint, identityPublicKeys)
    }

    fun createIdentityTopupTransition(identityId: Identifier, lockedOutpoint: TransactionOutPoint): IdentityTopupTransition {
        return  IdentityTopupTransition(identityId, lockedOutpoint.bitcoinSerialize())
    }

    fun applyIdentityCreateStateTransition(stateTransition: IdentityStateTransition) : Identity {

        val identityCreateTransition = stateTransition as IdentityCreateTransition

        val newIdentity = Identity(identityCreateTransition.identityId,
                0,
                identityCreateTransition.publicKeys, 0, Identity.PROTOCOL_VERSION)

        //store identity

        //

        return newIdentity

    }
}