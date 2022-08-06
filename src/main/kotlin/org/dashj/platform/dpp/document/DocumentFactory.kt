/**
 * Copyright (c) 2018-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashj.platform.dpp.document

import org.dashj.platform.dpp.DashPlatformProtocol
import org.dashj.platform.dpp.Factory
import org.dashj.platform.dpp.ProtocolVersion
import org.dashj.platform.dpp.StateRepository
import org.dashj.platform.dpp.contract.DataContract
import org.dashj.platform.dpp.document.errors.InvalidActionNameError
import org.dashj.platform.dpp.document.errors.InvalidInitialRevisionError
import org.dashj.platform.dpp.document.errors.MismatchOwnerIdsError
import org.dashj.platform.dpp.document.errors.NoDocumentsSuppliedError
import org.dashj.platform.dpp.errors.InvalidDocumentTypeError
import org.dashj.platform.dpp.identifier.Identifier
import org.dashj.platform.dpp.statetransition.StateTransition
import org.dashj.platform.dpp.util.Entropy
import org.dashj.platform.dpp.util.HashUtils

class DocumentFactory(dpp: DashPlatformProtocol, stateRepository: StateRepository) : Factory(dpp, stateRepository) {

    fun create(
        dataContract: DataContract,
        ownerId: Identifier,
        type: String,
        data: Map<String, Any?> = mapOf()
    ): Document {
        if (!dataContract.isDocumentDefined(type)) {
            throw InvalidDocumentTypeError(dataContract, type)
        }

        val documentEntropy = Entropy.generate()
        val dataContractId = dataContract.id

        val id = HashUtils.generateDocumentId(dataContractId.toBuffer(), ownerId.toBuffer(), type, documentEntropy)

        val rawDocument = hashMapOf<String, Any?>()
        rawDocument["\$protocolVersion"] = ProtocolVersion.latestVersion
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

        val document = Document(rawDocument, dataContract)
        document.entropy = documentEntropy
        return document
    }

    fun createFromObject(rawDocument: Map<String, Any?>, options: Options = Options()): Document {
        val dataContractId = Identifier.from(rawDocument["\$dataContractId"])
        val dataContract = stateRepository.fetchDataContract(dataContractId)
        return Document(rawDocument, dataContract!!)
    }

    fun createFromBuffer(payload: ByteArray, options: Options = Options()): Document {
        val (protocolVersion, rawDocument) = decodeProtocolEntity(payload)
        rawDocument["\$protocolVersion"] = protocolVersion
        return createFromObject(rawDocument, options)
    }

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
            if (it.ownerId != ownerId) {
                mismatches++
            }
        }

        if (mismatches > 0) {
            throw MismatchOwnerIdsError(documentsFlattened)
        }

        // Convert documents to action transitions
        val createDocuments = documents["create"] ?: listOf()
        val replaceDocuments = documents["replace"] ?: listOf()
        val deleteDocuments = documents["delete"] ?: listOf()

        val rawDocumentCreateTransitions = createDocuments.map {
            if (it.revision != DocumentCreateTransition.INITIAL_REVISION) {
                throw InvalidInitialRevisionError(it)
            }

            val rawTransition = hashMapOf<String, Any>()
            rawTransition["\$action"] = DocumentTransition.Action.CREATE.value
            rawTransition["\$id"] = it.id
            rawTransition["\$type"] = it.type
            rawTransition["\$dataContractId"] = it.dataContractId
            rawTransition["\$entropy"] = it.entropy
            if (it.createdAt != null) {
                rawTransition["\$createdAt"] = it.createdAt!!
            }
            if (it.updatedAt != null) {
                rawTransition["\$updatedAt"] = it.updatedAt!!
            }

            val dataKeys = it.data.keys.iterator()
            while (dataKeys.hasNext()) {
                val key = dataKeys.next()
                it.data[key]?.let { value -> rawTransition.put(key, value) }
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
            if (it.updatedAt != null) {
                rawTransition["\$updatedAt"] = it.updatedAt!!
            }
            val dataKeys = it.data.keys.iterator()
            while (dataKeys.hasNext()) {
                val key = dataKeys.next()
                it.data[key]?.let { value -> rawTransition.put(key, value) }
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

        val rawBatchTransition = hashMapOf<String, Any?>(
            "\$protocolVersion" to ProtocolVersion.latestVersion,
            "type" to StateTransition.Types.DOCUMENTS_BATCH.value,
            "ownerId" to ownerId,
            "transitions" to rawDocumentTransitions
        )

        val dataContracts = documentsFlattened.map {
            document ->
            document.dataContract
        }

        return DocumentsBatchTransition(dpp.getNetworkParameters(), rawBatchTransition, dataContracts)
    }
}
