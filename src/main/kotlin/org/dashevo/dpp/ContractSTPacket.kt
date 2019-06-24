/**
 * Copyright (c) 2018-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashevo.dpp

import org.dashevo.dpp.util.HashUtils
import org.json.JSONObject

class ContractSTPacket(contractId: String, val contract: Contract) : STPacket(contractId) {

    override fun getItemsMerkleRoot(): String {
        return contract.hash()
    }

    override fun getItemsHash(): String {
        val data = HashMap<String, List<Any>>()

        data["documents"] = listOf()
        data["contracts"] = listOf(this.contract)

        return HashUtils.toHash(data).toHexString()
    }

    override fun toJSON(): Map<String, Any> {
        val json = hashMapOf<String, Any>()

        json["contractId"] = contract.id
        json["itemsMerkleRoot"] = getItemsMerkleRoot()
        json["itemsHash"] = getItemsHash()
        json["contracts"] = listOf(contract.toJSON())
        json["documents"] = listOf<JSONObject>()

        return json
    }

}