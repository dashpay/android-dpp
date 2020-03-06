/**
 * Copyright (c) 2018-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashevo.dpp.document

import org.dashevo.dpp.errors.InvalidDocumentTypeError
import org.dashevo.dpp.util.HashUtils

class DocumentFactory(val userId: String, val contract: Contract) {

    fun create(type: String, data: Map<String, Any> = mapOf()) : Document {
        if (!this.contract.isDocumentDefined(type)) {
            throw InvalidDocumentTypeError(contract, type)
        }

        val scope = HashUtils.toHash((contract.id + userId).toByteArray())
        val rawDocument = hashMapOf<String, Any>()
        rawDocument.put("\$type", type)
        rawDocument.put("\$scope", scope.toHexString())
        rawDocument.put("\$scopeId", HashUtils.createScopeId())
        rawDocument.put("\$action", Document.ACTION.ordinal)
        rawDocument.put("\$rev", Document.REVISION)
        rawDocument.put("\$meta", mapOf("userId" to this.userId))

        val dataKeys = data.keys.iterator()
        while (dataKeys.hasNext()) {
            val key = dataKeys.next()
            data.get(key)?.let { rawDocument.put(key, it) }
        }

        return Document(rawDocument)
    }

    fun createFromObject(rawDocument: MutableMap<String, Any>, options: Options = Options()): Document {
        return Document(rawDocument)
    }

    fun createFromSerialized(payload: ByteArray, options: Options = Options()): Document {
        val rawDocument = HashUtils.decode(payload).toMutableMap()
        return createFromObject(rawDocument)
    }

    inner class Options(skipValidation: Boolean = false)

}