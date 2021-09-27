/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashj.platform.dpp.identity

import org.dashj.platform.dpp.ProtocolVersion
import org.dashj.platform.dpp.identifier.Identifier
import org.dashj.platform.dpp.statetransition.StateTransitionIdentitySigned

class StateTransitionMock(protocolVersion: Int = ProtocolVersion.latestVersion) : StateTransitionIdentitySigned(Types.DATA_CONTRACT_CREATE, protocolVersion) {
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
