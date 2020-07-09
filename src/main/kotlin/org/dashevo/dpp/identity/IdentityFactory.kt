/**
 * Copyright (c) 2018-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashevo.dpp.identity

import org.bitcoinj.core.TransactionOutPoint
import org.dashevo.dpp.Factory
import org.dashevo.dpp.util.Cbor

class IdentityFactory : Factory() {

    fun create(lockedOutPoint: TransactionOutPoint, publicKeys: List<IdentityPublicKey>) : Identity {
        val id = lockedOutPoint.hash.toStringBase58()
        return Identity(id, publicKeys)
    }

    fun create(id: String, publicKeys: List<IdentityPublicKey>) : Identity {
        return Identity(id, publicKeys)
    }

    fun createFromObject(rawIdentity: MutableMap<String, Any?>, options: Options = Options()): Identity {
        return Identity(rawIdentity)
    }

    fun createFromSerialized(payload: ByteArray, options: Options = Options()): Identity {
        val rawIdentity = Cbor.decode(payload).toMutableMap()
        return createFromObject(rawIdentity, options)
    }

    fun createIdentityCreateTransition(identity: Identity): IdentityCreateTransition {
        val lockedOutpoint = identity.lockedOutpoint!!.toStringBase64()

        return  IdentityCreateTransition(lockedOutpoint, identity.publicKeys)
    }

    fun createIdentityCreateTransition(lockedOutpoint: String, identityPublicKeys: List<IdentityPublicKey>): IdentityCreateTransition {

        return  IdentityCreateTransition(lockedOutpoint, identityPublicKeys)
    }

    fun createIdentityTopupTransition(identityId: String, lockedOutpoint: TransactionOutPoint): IdentityTopupTransition {
        val lockedOutpointString = lockedOutpoint.toStringBase64()

        return  IdentityTopupTransition(identityId, lockedOutpointString)
    }

    fun applyIdentityCreateStateTransition(stateTransition: IdentityStateTransition) : Identity {

        val identityCreateTransition = stateTransition as IdentityCreateTransition

        val newIdentity = Identity(identityCreateTransition.identityId,
                0,
                identityCreateTransition.publicKeys)

        //store identity

        //

        return newIdentity

    }
}