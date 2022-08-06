/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashj.platform.dpp.document

import org.dashj.platform.dpp.contract.DataContract

class DocumentReplaceTransition(rawStateTransition: MutableMap<String, Any?>, dataContract: DataContract) :
    DataDocumentTransition(rawStateTransition, dataContract) {

    override val action = Action.REPLACE
    var updatedAt: Long?
    var revision: Int

    init {
        this.updatedAt = rawStateTransition["\$updatedAt"]?.let { it as Long }
        this.revision = rawStateTransition.remove("\$revision") as Int
    }

    override fun toObject(skipIdentifierConversion: Boolean): MutableMap<String, Any?> {
        val map = super.toObject(skipIdentifierConversion)
        map["\$revision"] = revision
        updatedAt?.let { map["\$updatedAt"] = it }
        return map
    }
}
