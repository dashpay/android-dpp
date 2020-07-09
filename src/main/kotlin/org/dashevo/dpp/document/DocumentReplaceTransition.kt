/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashevo.dpp.document

class DocumentReplaceTransition : DocumentCreateTransition {

    override val action = Action.REPLACE
    var revision: Int

    constructor(rawStateTransition: MutableMap<String, Any?>) : super(rawStateTransition) {
        this.revision = rawStateTransition.remove("\$revision") as Int
    }

    override fun toJSON(): Map<String, Any> {
        var json = super.toJSON() as MutableMap<String, Any>
        json["\$revision"] = revision

        return json
    }
}