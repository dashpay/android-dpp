/**
 * Copyright (c) 2018-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashevo.dpp.identity

import org.dashevo.dpp.Factory
import org.dashevo.dpp.errors.IdentityAlreadyExistsError
import org.dashevo.dpp.identity.errors.WrongStateTransitionTypeError
import org.dashevo.dpp.statetransition.StateTransition
import org.dashevo.dpp.util.HashUtils

class IdentityFactory() : Factory() {

    fun create(id: String, type: Identity.IdentityType, publicKeys: List<IdentityPublicKey>) : Identity {
        return Identity(id, type, publicKeys)
    }

    fun createFromObject(rawIdentity: MutableMap<String, Any>, options: Options = Options()): Identity {
        return Identity(rawIdentity)
    }

    fun createFromSerialized(payload: ByteArray, options: Options = Options()): Identity {
        val rawIdentity = HashUtils.decode(payload).toMutableMap()
        return createFromObject(rawIdentity, options)
    }

    fun applyCreateStateTransition(stateTransition: IdentityStateTransition) : Identity {
        return applyIdentityStateTransition(stateTransition, null)
    }

    fun applyStateTransition(stateTransition: IdentityStateTransition, identity: Identity?) : Identity {
        return applyIdentityStateTransition(stateTransition, identity)
    }

    fun applyIdentityStateTransition(stateTransition: IdentityStateTransition, identity: Identity?) : Identity {
        // noinspection JSRedundantSwitchStatement
        when (stateTransition.type) {
            StateTransition.Types.IDENTITY_CREATE -> {
                val identityCreateTransition = stateTransition as IdentityCreateTransition
                if (identity != null) {
                    throw IdentityAlreadyExistsError (stateTransition)
                }

                val newIdentity = Identity(identityCreateTransition.identityId,
                        identityCreateTransition.identityType!!, identityCreateTransition.publicKeys);

                return newIdentity
            }
            else -> {
                throw WrongStateTransitionTypeError(stateTransition);
            }
        }
    }
}