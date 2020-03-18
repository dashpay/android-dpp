package org.dashevo.dpp.contract

import org.dashevo.dpp.statetransition.StateTransition

class ContractStateTransition(var contract: Contract) : StateTransition(Types.DATA_CONTRACT) {

    override fun toJSON(skipSignature: Boolean): Map<String, Any> {
        var json = super.toJSON(skipSignature) as MutableMap<String, Any>
        json["dataContract"] = contract
        return json
    }
}