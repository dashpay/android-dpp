package org.dashevo.dpp.contract

import org.dashevo.dpp.identifier.Identifier
import org.dashevo.dpp.statetransition.StateTransitionIdentitySigned
import org.dashevo.dpp.toBase64
import org.dashevo.dpp.util.Entropy
import org.dashevo.dpp.util.HashUtils.byteArrayfromBase64orByteArray

class DataContractCreateTransition : StateTransitionIdentitySigned {

    var dataContract: DataContract
    var entropy: ByteArray

    constructor(dataContract: DataContract) : super(Types.DATA_CONTRACT_CREATE) {
        this.dataContract = dataContract

        if (dataContract.entropy == null) {
            dataContract.entropy = Entropy.generate()
        }
        this.entropy = dataContract.entropy!!
    }
    /** returns id of created contract */
    override val modifiedDataIds: List<Identifier>
        get() = listOf(dataContract.id)

    constructor(rawStateTransition: MutableMap<String, Any?>) : super(rawStateTransition) {
        dataContract = DataContract(rawStateTransition["dataContract"] as MutableMap<String, Any?>)
        entropy = byteArrayfromBase64orByteArray(rawStateTransition["entropy"]!!)
    }

    override fun toObject(skipSignature: Boolean, skipIdentifiersConversion: Boolean): MutableMap<String, Any?> {
        val json = super.toObject(skipSignature, skipIdentifiersConversion)
        json["dataContract"] = dataContract.toObject()
        json["entropy"] = entropy
        return json
    }

    override fun toJSON(skipSignature: Boolean): MutableMap<String, Any?> {
        val json = super.toJSON(skipSignature)
        json["dataContract"] = dataContract.toJSON()
        json["entropy"] = entropy.toBase64()
        return json
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