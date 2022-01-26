/**
 * Copyright (c) 2022-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashj.platform.dpp.contract

import org.bitcoinj.core.NetworkParameters
import org.dashj.platform.dpp.identifier.Identifier
import org.dashj.platform.dpp.statetransition.StateTransitionIdentitySigned

abstract class DataContractTransition : StateTransitionIdentitySigned {

    var dataContract: DataContract

    constructor(params: NetworkParameters, dataContract: DataContract, type: Types) : super(params, type) {
        this.dataContract = dataContract
    }
    /** returns id of created contract */
    override val modifiedDataIds: List<Identifier>
        get() = listOf(dataContract.id)

    constructor(params: NetworkParameters, rawStateTransition: MutableMap<String, Any?>) :
        super(params, rawStateTransition) {
            dataContract = DataContract(rawStateTransition["dataContract"] as MutableMap<String, Any?>)
        }

    override fun isDataContractStateTransition(): Boolean {
        return true
    }

    override fun isDocumentStateTransition(): Boolean {
        return false
    }

    override fun isIdentityStateTransition(): Boolean {
        return false
    }
}
