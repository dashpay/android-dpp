/**
 * Copyright (c) 2018-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashevo.dpp

class ContractFactory {

    fun create(rawDocument: MutableMap<String, Any>): Contract {

        val contract = Contract(rawDocument.get("name") as String,
                rawDocument.get("documents") as MutableMap<String, Any>)

        if (rawDocument.containsKey("\$schema")) {
            contract.schema = rawDocument.get("\$schema") as String
        }

        if (rawDocument.containsKey("version")) {
            contract.version = rawDocument.get("version") as Double
        }

        if (rawDocument.containsKey("definitions")) {
            contract.definitions = rawDocument.get("definitions") as MutableMap<String, Any>
        }

        return contract
    }

}