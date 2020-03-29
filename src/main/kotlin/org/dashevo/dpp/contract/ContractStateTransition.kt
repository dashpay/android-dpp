package org.dashevo.dpp.contract

import org.dashevo.dpp.statetransition.StateTransition

class ContractStateTransition : StateTransition {

    var contract: Contract

    constructor(contract: Contract) : super(Types.DATA_CONTRACT) {
        this.contract = contract
    }

    constructor(rawStateTransition: MutableMap<String, Any?>) : super(rawStateTransition) {
        contract = Contract(rawStateTransition["dataContract"] as MutableMap<String, Any?>)
    }

    override fun toJSON(skipSignature: Boolean): Map<String, Any> {
        var json = super.toJSON(skipSignature) as MutableMap<String, Any>
        json["dataContract"] = contract.toJSON()
        return json
    }
}