/**
 * Copyright (c) 2018-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashevo.dpp.document

import org.dashevo.dpp.Factory
import org.dashevo.dpp.contract.DataContract
import org.dashevo.dpp.document.errors.InvalidActionNameError
import org.dashevo.dpp.document.errors.InvalidInitialRevisionError
import org.dashevo.dpp.document.errors.MismatchOwnerIdsError
import org.dashevo.dpp.document.errors.NoDocumentsSuppliedError
import org.dashevo.dpp.errors.InvalidDocumentTypeError
import org.dashevo.dpp.statetransition.StateTransition
import org.dashevo.dpp.util.Cbor
import org.dashevo.dpp.util.Entropy
import org.dashevo.dpp.util.HashUtils

class DocumentFactory : Factory() {

    fun create(dataContract: DataContract, ownerId: String, type: String, data: Map<String, Any> = mapOf()) : Document {
        if (!dataContract.isDocumentDefined(type)) {
            throw InvalidDocumentTypeError(dataContract, type)
        }

        val documentEntropy = Entropy.generate()
        val dataContractId = dataContract.id

        val id = HashUtils.generateDocumentId(dataContractId, ownerId, type, documentEntropy)

        val rawDocument = hashMapOf<String, Any?>()
        rawDocument["\$id"] = id
        rawDocument["\$type"] = type
        rawDocument["\$dataContractId"] = dataContract.id
        rawDocument["\$ownerId"] = ownerId
        rawDocument["\$revision"] = DocumentCreateTransition.INITIAL_REVISION


        val dataKeys = data.keys.iterator()
        while (dataKeys.hasNext()) {
            val key = dataKeys.next()
            data[key]?.let { rawDocument.put(key, it) }
        }

        val document = Document(rawDocument)
        document.entropy = documentEntropy
        return document
    }

    fun createFromObject(rawDocument: MutableMap<String, Any?>, options: Options = Options()): Document {
        return Document(rawDocument)
    }

    fun createFromSerialized(payload: ByteArray, options: Options = Options()): Document {
        val rawDocument = Cbor.decode(payload).toMutableMap()
        return createFromObject(rawDocument, options)
    }
    
    /*fun createStateTransition(documents: List<Document>) : DocumentsStateTransition {
        return DocumentsStateTransition(documents)
    }*/

    fun createStateTransition(documents: Map<String, List<Document>?>): DocumentsBatchTransition {
        // Check no wrong actions were supplied
        val allowedKeys = DocumentTransition.Action.getValidNames()

        val actionKeys = documents.keys
        val filteredKeys = actionKeys.filter { allowedKeys.indexOf(it) == -1 }

        if (filteredKeys.isNotEmpty()) {
            throw InvalidActionNameError(filteredKeys)
        }

        val documentsFlattened = ArrayList<Document>()

        for (key in actionKeys) {
            documentsFlattened.addAll(documents[key] as List<Document>)
        }

        if (documentsFlattened.isEmpty()) {
            throw NoDocumentsSuppliedError()
        }

        // Check that documents are not mixed
        val aDocument = documentsFlattened[0]

        val ownerId = aDocument.ownerId

        var mismatches = 0
        documentsFlattened.forEach {
            if(it.ownerId != ownerId)
                mismatches++
        }

        if (mismatches > 0) {
            throw MismatchOwnerIdsError(documentsFlattened)
        }

        // Convert documents to action transitions
        val createDocuments = documents["create"] ?: listOf()
        val replaceDocuments = documents["replace"] ?: listOf()
        val deleteDocuments = documents["delete"] ?: listOf()

        val rawDocumentCreateTransitions = createDocuments.map {
            if (it.revision !== DocumentCreateTransition.INITIAL_REVISION) {
                throw InvalidInitialRevisionError(it)
            }

            val rawTransition = hashMapOf<String, Any>()
            rawTransition["\$action"] = DocumentTransition.Action.CREATE.value
            rawTransition["\$id"] = it.id
            rawTransition["\$type"] = it.type
            rawTransition["\$dataContractId"] = it.dataContractId
            rawTransition["\$entropy"] = it.entropy
            if (it.createdAt != null)
                rawTransition["\$createdAt"] = it.createdAt!!

            val dataKeys = it.data.keys.iterator()
            while (dataKeys.hasNext()) {
                val key = dataKeys.next()
                it.data[key]?.let { rawTransition.put(key, it) }
            }
            rawTransition
        }

        val rawDocumentReplaceTransitions = replaceDocuments.map {
            val rawTransition = hashMapOf<String, Any>()
            rawTransition["\$action"] = DocumentTransition.Action.REPLACE.value
            rawTransition["\$id"] = it.id
            rawTransition["\$type"] = it.type
            rawTransition["\$dataContractId"] = it.dataContractId
            rawTransition["\$revision"] = it.revision + 1
            if (it.updatedAt != null)
                rawTransition["\$createdAt"] = it.updatedAt!!

            val dataKeys = it.data.keys.iterator()
            while (dataKeys.hasNext()) {
                val key = dataKeys.next()
                it.data[key]?.let { rawTransition.put(key, it) }
            }
            rawTransition
        }

        val rawDocumentDeleteTransitions = deleteDocuments.map {
            val rawTransition = hashMapOf<String, Any>()
            rawTransition["\$action"] = DocumentTransition.Action.DELETE.value
            rawTransition["\$id"] = it.id
            rawTransition["\$type"] = it.type
            rawTransition["\$dataContractId"] = it.dataContractId
            rawTransition
        }

        val rawDocumentTransitions: ArrayList<Any> = arrayListOf()
        rawDocumentTransitions.addAll(rawDocumentCreateTransitions)
        rawDocumentTransitions.addAll(rawDocumentReplaceTransitions)
        rawDocumentTransitions.addAll(rawDocumentDeleteTransitions)

        val rawBatchTransition = hashMapOf<String, Any?> (
                "type" to StateTransition.Types.DOCUMENTS_BATCH.value,
                "ownerId" to ownerId,
                "transitions" to rawDocumentTransitions
        )
        return DocumentsBatchTransition(rawBatchTransition)
    }
}