package org.dashevo.dpp.contract

import org.dashevo.dpp.statetransition.StateTransitionIdentitySigned

class ContractStateTransition : StateTransitionIdentitySigned {

    var dataContract: DataContract

    constructor(dataContract: DataContract) : super(Types.DATA_CONTRACT_CREATE) {
        this.dataContract = dataContract
    }

    constructor(rawStateTransition: MutableMap<String, Any?>) : super(rawStateTransition) {
        dataContract = DataContract(rawStateTransition["dataContract"] as MutableMap<String, Any?>)
    }

    override fun toJSON(skipSignature: Boolean): MutableMap<String, Any?> {
        var json = super.toJSON(skipSignature)
        json["dataContract"] = dataContract.toJSON()
        return json
    }
}