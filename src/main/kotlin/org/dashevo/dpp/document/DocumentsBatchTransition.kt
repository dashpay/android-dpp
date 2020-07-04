package org.dashevo.dpp.document

import org.dashevo.dpp.statetransition.StateTransition
import org.dashevo.dpp.statetransition.StateTransitionIdentitySigned
import java.lang.IllegalStateException

class DocumentsBatchTransition : StateTransitionIdentitySigned {

    var ownerId: String
    var transitions: List<DocumentTransition>

    constructor(ownerId: String, transitions: List<DocumentTransition>): super(StateTransition.Types.DOCUMENTS_BATCH) {
        this.ownerId = ownerId
        this.transitions = transitions
    }

    constructor(rawStateTransition: MutableMap<String, Any?>): super(rawStateTransition) {
        ownerId = rawStateTransition["ownerId"] as String
        transitions = (rawStateTransition["transitions"] as List<Any?>).map {
            when(((it as Map<String, Any?>)["\$action"] as Int)) {
                DocumentTransition.Action.CREATE.value -> DocumentCreateTransition(it as MutableMap<String, Any?>)
                DocumentTransition.Action.REPLACE.value -> DocumentReplaceTransition(it as MutableMap<String, Any?>)
                DocumentTransition.Action.DELETE.value -> DocumentReplaceTransition(it as MutableMap<String, Any?>)
                else -> throw IllegalStateException("Invalid action")
            }
        }
    }

    override fun toJSON(skipSignature: Boolean): Map<String, Any> {
        var json = super.toJSON(skipSignature) as MutableMap<String, Any>
        json["ownerId"] = ownerId
        json["transitions"] = transitions.map { entry -> entry.toJSON() }
        return json
    }
}