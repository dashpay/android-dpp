/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashevo.dpp.document

import org.dashevo.dpp.statetransition.StateTransition
import org.dashevo.dpp.statetransition.StateTransitionIdentitySigned
import java.lang.IllegalStateException

class DocumentsBatchTransition : StateTransitionIdentitySigned {

    var ownerId: String
    var transitions: List<DocumentTransition>

    constructor(ownerId: String, transitions: List<DocumentTransition>) : super(Types.DOCUMENTS_BATCH) {
        this.ownerId = ownerId
        this.transitions = transitions
    }

    constructor(rawStateTransition: MutableMap<String, Any?>) : super(rawStateTransition) {
        ownerId = rawStateTransition["ownerId"] as String
        transitions = (rawStateTransition["transitions"] as List<Any?>).map {
            when (((it as MutableMap<String, Any?>)["\$action"] as Int)) {
                DocumentTransition.Action.CREATE.value -> DocumentCreateTransition(it)
                DocumentTransition.Action.REPLACE.value -> DocumentReplaceTransition(it)
                DocumentTransition.Action.DELETE.value -> DocumentReplaceTransition(it)
                else -> throw IllegalStateException("Invalid action")
            }
        }
    }

    override fun toJSON(skipSignature: Boolean): MutableMap<String, Any?> {
        var json = super.toJSON(skipSignature)
        json["ownerId"] = ownerId
        json["transitions"] = transitions.map { entry -> entry.toJSON() }
        return json
    }
}