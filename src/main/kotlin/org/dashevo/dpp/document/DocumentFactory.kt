/**
 * Copyright (c) 2018-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashevo.dpp.document

import org.dashevo.dpp.Factory
import org.dashevo.dpp.contract.DataContract
import org.dashevo.dpp.errors.InvalidDocumentTypeError
import org.dashevo.dpp.util.Cbor
import org.dashevo.dpp.util.Entropy

class DocumentFactory() : Factory() {

    fun create(dataContract: DataContract, userId: String, type: String, data: Map<String, Any> = mapOf()) : Document {
        if (!dataContract.isDocumentDefined(type)) {
            throw InvalidDocumentTypeError(dataContract, type)
        }

        val rawDocument = hashMapOf<String, Any?>()
        rawDocument.put("\$type", type)
        rawDocument.put("\$contractId", dataContract.contractId)
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

    fun createFromObject(rawDocument: MutableMap<String, Any?>, options: Options = Options()): Document {
        return Document(rawDocument)
    }

    fun createFromSerialized(payload: ByteArray, options: Options = Options()): Document {
        val rawDocument = Cbor.decode(payload).toMutableMap()
        return createFromObject(rawDocument, options)
    }

    fun createStateTransition(documents: List<Document>) : DocumentsStateTransition {
        return DocumentsStateTransition(documents)
    }
}