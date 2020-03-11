/**
 * Copyright (c) 2018-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashevo.dpp.document

import org.dashevo.dpp.Factory
import org.dashevo.dpp.contract.Contract
import org.dashevo.dpp.errors.InvalidDocumentTypeError
import org.dashevo.dpp.util.Entropy
import org.dashevo.dpp.util.HashUtils

class DocumentFactory() : Factory() {

    fun create(contract: Contract, userId: String, type: String, data: Map<String, Any> = mapOf()) : Document {
        if (!contract.isDocumentDefined(type)) {
            throw InvalidDocumentTypeError(contract, type)
        }

        val rawDocument = hashMapOf<String, Any>()
        rawDocument.put("\$type", type)
        rawDocument.put("\$contractId", contract.contractId)
        rawDocument.put("\$userId", userId)
        rawDocument.put("\$entropy", Entropy.generate())
        rawDocument.put("\$rev", Document.REVISION)

        val dataKeys = data.keys.iterator()
        while (dataKeys.hasNext()) {
            val key = dataKeys.next()
            data[key]?.let { rawDocument.put(key, it) }
        }

        val document = Document(rawDocument)
        document.action = Document.ACTION
        return document
    }

    fun createFromObject(rawDocument: MutableMap<String, Any>, options: Options = Options()): Document {
        return Document(rawDocument)
    }

    fun createFromSerialized(payload: ByteArray, options: Options = Options()): Document {
        val rawDocument = HashUtils.decode(payload).toMutableMap()
        return createFromObject(rawDocument, options)
    }
}