/**
 * Copyright (c) 2018-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashevo.dpp

import org.dashevo.dpp.document.Document
import org.dashevo.dpp.util.HashUtils

class DocumentsSTPacket(contractId: String, val documents: List<Document>) : STPacket(contractId) {

    override fun getItemsMerkleRoot(): String {
        val hashes = documents.map { it.hash() }
        return HashUtils.getMerkleRoot(HashUtils.getMerkleTree(hashes)).toHexString()
    }

    override fun getItemsHash(): String {
        val data = HashMap<String, List<ByteArray>>()

        data["documents"] = documents.map { it.hash() }
        data["contracts"] = listOf()

        return HashUtils.toHash(data).toHexString()
    }

    override fun toJSON(): Map<String, Any> {
        val json = hashMapOf<String, Any>()

        json.put("contractId", contractId)
        json.put("itemsMerkleRoot", getItemsMerkleRoot())
        json.put("itemsHash", getItemsHash())
        json.put("contracts", listOf<Map<String, Any>>())
        json.put("documents", documents.map { it.toJSON() })

        return json
    }

}