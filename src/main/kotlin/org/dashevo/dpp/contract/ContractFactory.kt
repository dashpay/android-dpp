/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashevo.dpp.contract

import org.dashevo.dpp.Factory
import org.dashevo.dpp.util.Cbor

class ContractFactory : Factory() {

    fun createDataContract(rawDataContract: MutableMap<String, Any?>): DataContract {

        val contractId = if (rawDataContract.containsKey("contractId")) rawDataContract["contractId"] as String else ""

        val contract = DataContract(contractId,
                rawDataContract["documents"] as MutableMap<String, Any?>)

        if (rawDataContract.containsKey("\$schema")) {
            contract.schema = rawDataContract["\$schema"] as String
        }

        if (rawDataContract.containsKey("version")) {
            contract.version = rawDataContract["version"] as Int
        }

        if (rawDataContract.containsKey("definitions")) {
            contract.definitions = rawDataContract["definitions"] as MutableMap<String, Any>
        }

        return contract
    }

    fun create(contractId: String, documents: MutableMap<String, Any?>): DataContract {
        val rawContract = HashMap<String, Any?>(2)
        rawContract["contractId"] = contractId
        rawContract["documents"] = documents
        return createDataContract(rawContract)
    }

    fun createFromObject(rawContract: MutableMap<String, Any?>, options: Options = Options()): DataContract {
        return createDataContract(rawContract)
    }

    fun createFromSerialized(payload: ByteArray, options: Options = Options()): DataContract {
        val rawDocument = Cbor.decode(payload).toMutableMap()
        return createFromObject(rawDocument, options)
    }

    fun createStateTransition(dataContract: DataContract) : ContractStateTransition {
        return ContractStateTransition(dataContract)
    }

}