package org.dashj.platform.dpp.contract

import org.bitcoinj.core.NetworkParameters

class DataContractUpdateTransition : DataContractTransition {

    constructor(params: NetworkParameters, dataContract: DataContract) :
        super(params, dataContract, Types.DATA_CONTRACT_CREATE) {
            this.dataContract = dataContract
        }

    constructor(params: NetworkParameters, rawStateTransition: MutableMap<String, Any?>) :
        super(params, rawStateTransition) {
            dataContract = DataContract(rawStateTransition["dataContract"] as MutableMap<String, Any?>)
        }

    override fun toObject(skipSignature: Boolean, skipIdentifiersConversion: Boolean): MutableMap<String, Any?> {
        val json = super.toObject(skipSignature, skipIdentifiersConversion)
        json["dataContract"] = dataContract.toObject()
        return json
    }

    override fun toJSON(skipSignature: Boolean): MutableMap<String, Any?> {
        val json = super.toJSON(skipSignature)
        json["dataContract"] = dataContract.toJSON()
        return json
    }
}
