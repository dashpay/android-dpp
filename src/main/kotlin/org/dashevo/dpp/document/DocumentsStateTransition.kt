package org.dashevo.dpp.document

import org.dashevo.dpp.statetransition.StateTransition

class DocumentsStateTransition : StateTransition {

    var documents: List<Document>

    constructor(documents: List<Document>): super(StateTransition.Types.DOCUMENTS) {
        this.documents = documents
    }

    constructor(rawStateTransition: MutableMap<String, Any?>): super(rawStateTransition) {
        documents = (rawStateTransition["documents"] as List<Any?>).map { Document(it as MutableMap<String, Any?>)}
    }

    override fun toJSON(skipSignature: Boolean): Map<String, Any> {
        var json = super.toJSON(skipSignature) as MutableMap<String, Any>
        json["actions"] = documents.map { entry -> entry.action.value }
        json["documents"] = documents.map { entry -> entry.toJSON() }
        return json
    }
}