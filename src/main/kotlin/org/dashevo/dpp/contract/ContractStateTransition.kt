package org.dashevo.dpp.contract

import org.dashevo.dpp.statetransition.StateTransition

class ContractStateTransition : StateTransition {

    var dataContract: DataContract

    constructor(dataContract: DataContract) : super(Types.DATA_CONTRACT) {
        this.dataContract = dataContract
    }

    constructor(rawStateTransition: MutableMap<String, Any?>) : super(rawStateTransition) {
        dataContract = DataContract(rawStateTransition["dataContract"] as MutableMap<String, Any?>)
    }

    override fun toJSON(skipSignature: Boolean): Map<String, Any> {
        var json = super.toJSON(skipSignature) as MutableMap<String, Any>
        json["dataContract"] = dataContract.toJSON()
        return json
    }
}