package org.dashevo.dpp.document

import org.dashevo.dpp.statetransition.StateTransition

class DocumentsStateTransition(var documents: List<Document>): StateTransition(Types.DATA_CONTRACT) {

    override fun toJSON(skipSignature: Boolean): Map<String, Any> {
        var json = super.toJSON() as MutableMap<String, Any>
        json["actions"] = documents.map { entry -> entry.action }
        json["documents"] = documents.map { entry -> entry.toJSON() }
        return json
    }
}