package org.dashj.platform.dpp.contract

import org.bitcoinj.core.NetworkParameters
import org.dashj.platform.dpp.identifier.Identifier
import org.dashj.platform.dpp.toBase64
import org.dashj.platform.dpp.util.Converters
import org.dashj.platform.dpp.util.Entropy

class DataContractCreateTransition : DataContractTransition {

    var entropy: ByteArray

    constructor(params: NetworkParameters, dataContract: DataContract) :
        super(params, dataContract, Types.DATA_CONTRACT_CREATE) {
            this.dataContract = dataContract

            if (dataContract.entropy == null) {
                dataContract.entropy = Entropy.generate()
            }
            this.entropy = dataContract.entropy!!
        }
    /** returns id of created contract */
    override val modifiedDataIds: List<Identifier>
        get() = listOf(dataContract.id)

    constructor(params: NetworkParameters, rawStateTransition: MutableMap<String, Any?>) :
        super(params, rawStateTransition) {
            dataContract = DataContract(rawStateTransition["dataContract"] as MutableMap<String, Any?>)
            entropy = Converters.byteArrayFromBase64orByteArray(rawStateTransition["entropy"]!!)
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
