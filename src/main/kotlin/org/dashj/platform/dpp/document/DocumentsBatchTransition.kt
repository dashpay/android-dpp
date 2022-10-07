/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashj.platform.dpp.document

import org.bitcoinj.core.NetworkParameters
import org.dashj.platform.dpp.contract.DataContract
import org.dashj.platform.dpp.identifier.Identifier
import org.dashj.platform.dpp.identity.IdentityPublicKey
import org.dashj.platform.dpp.statetransition.StateTransitionIdentitySigned
import java.lang.IllegalStateException

class DocumentsBatchTransition : StateTransitionIdentitySigned {

    var ownerId: Identifier
    var transitions: List<DocumentTransition>

    /** returns ids of all affected documents */
    override val modifiedDataIds: List<Identifier>
        get() = transitions.map { it.id }

    constructor(params: NetworkParameters, ownerId: Identifier, transitions: List<DocumentTransition>) :
        super(params, Types.DOCUMENTS_BATCH) {
            this.ownerId = ownerId
            this.transitions = transitions
        }

    constructor(
        params: NetworkParameters,
        rawStateTransition: MutableMap<String, Any?>,
        dataContracts: List<DataContract>
    ) :
        super(params, rawStateTransition) {
            ownerId = Identifier.from(rawStateTransition["ownerId"])
            val dataContractsMap = dataContracts.associateBy({ it.id }, { it })
            transitions = (rawStateTransition["transitions"] as List<Any?>).map { rawDocumentTransition ->
                when (((rawDocumentTransition as MutableMap<String, Any?>)["\$action"] as Int)) {
                    DocumentTransition.Action.CREATE.value -> DocumentCreateTransition(
                        rawDocumentTransition,
                        dataContractsMap[Identifier.from(rawDocumentTransition["\$dataContractId"])]!!
                    )

                    DocumentTransition.Action.REPLACE.value -> DocumentReplaceTransition(
                        rawDocumentTransition,
                        dataContractsMap[Identifier.from(rawDocumentTransition["\$dataContractId"])]!!
                    )

                    DocumentTransition.Action.DELETE.value -> DocumentDeleteTransition(
                        rawDocumentTransition,
                        dataContractsMap[Identifier.from(rawDocumentTransition["\$dataContractId"])]!!
                    )

                    else -> throw IllegalStateException("Invalid action")
                }
            }
        }

    override fun toObject(skipSignature: Boolean, skipIdentifiersConversion: Boolean): MutableMap<String, Any?> {
        val map = super.toObject(skipSignature, skipIdentifiersConversion)
        map["ownerId"] = ownerId
        map["transitions"] = transitions.map { entry -> entry.toObject() }

        if (!skipIdentifiersConversion) {
            map["ownerId"] = ownerId.toBuffer()
        }
        return map
    }

    override fun toJSON(skipSignature: Boolean): MutableMap<String, Any?> {
        val json = super.toJSON(skipSignature)
        json["ownerId"] = ownerId.toString()
        json["transitions"] = transitions.map { entry -> entry.toJSON() }
        return json
    }

    override fun isDataContractStateTransition(): Boolean {
        return false
    }

    override fun isDocumentStateTransition(): Boolean {
        return true
    }

    override fun isIdentityStateTransition(): Boolean {
        return false
    }

    /**
     * Returns minimal key security level that can be used to sign this ST
     */
    override fun getKeySecurityLevelRequirement(): IdentityPublicKey.SecurityLevel {
        val defaultSecurityLevel = IdentityPublicKey.SecurityLevel.HIGH

        // Step 1: Get all document types for the ST
        // Step 2: Get document schema for every type
        // If schema has security level, use that, if not, use the default security level
        // Find the highest level (lowest int value) of all documents - the ST's signature
        // requirement is the highest level across all documents affected by the ST.
        val documentTransitions = this.transitions
        var highestSecurityLevel: IdentityPublicKey.SecurityLevel? = null
        documentTransitions.forEach { documentTransition ->
            val documentType = documentTransition.type
            val dataContract = documentTransition.dataContract
            val documentSchema = dataContract.getDocumentSchema(documentType)

            val documentKeySecurityLevelRequirement = IdentityPublicKey.SecurityLevel.getByCode(
                (documentSchema["signatureSecurityLevelRequirement"] as? Int) ?: defaultSecurityLevel.value
            )

            if (highestSecurityLevel == null || highestSecurityLevel!! > documentKeySecurityLevelRequirement) {
                highestSecurityLevel = documentKeySecurityLevelRequirement
            }
        }

        return highestSecurityLevel!!
    }
}
