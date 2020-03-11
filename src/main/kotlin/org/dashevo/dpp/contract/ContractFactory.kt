/**
 * Copyright (c) 2018-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashevo.dpp.contract

import org.dashevo.dpp.Factory

class ContractFactory : Factory() {

    fun createDataContract(rawDataContract: MutableMap<String, Any>): Contract {

        val contractId = if (rawDataContract.containsKey("contractId")) rawDataContract["contractId"] as String else ""

        val contract = Contract(contractId,
                rawDataContract["documents"] as MutableMap<String, Any>)

        if (rawDataContract.containsKey("\$schema")) {
            contract.schema = rawDataContract["\$schema"] as String
        }

        if (rawDocument.containsKey("version")) {
            contract.version = rawDocument.get("version") as Int
        }

        if (rawDocument.containsKey("definitions")) {
            contract.definitions = rawDocument.get("definitions") as MutableMap<String, Any>
        }

        return contract
    }

}