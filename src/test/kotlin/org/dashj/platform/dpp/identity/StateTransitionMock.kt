/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashevo.dpp.identity

import org.dashevo.dpp.identifier.Identifier
import org.dashevo.dpp.statetransition.StateTransitionIdentitySigned

class StateTransitionMock : StateTransitionIdentitySigned(Types.DATA_CONTRACT_CREATE, 0) {
    override val modifiedDataIds: List<Identifier>
        get() = TODO("Not yet implemented")

    override fun isDocumentStateTransition(): Boolean {
        return false
    }

    override fun isDataContractStateTransition(): Boolean {
        return true
    }

    override fun isIdentityStateTransition(): Boolean {
        return false
    }

}