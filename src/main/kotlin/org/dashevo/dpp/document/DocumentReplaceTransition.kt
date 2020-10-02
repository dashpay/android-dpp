/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashevo.dpp.document

class DocumentReplaceTransition : DocumentTransition {

    override val action = Action.REPLACE
    var id: String
    var documentType: String
    var data: Map<String, Any?>
    var createdAt: Long?
    var updatedAt: Long?
    var revision: Int

    constructor(rawStateTransition: MutableMap<String, Any?>) : super(rawStateTransition) {
        val data = HashMap(rawStateTransition)

        this.id = data.remove("\$id") as String
        this.documentType = data.remove("\$type") as String
        data.remove("\$action")
        data.remove("\$dataContractId") as String
        this.createdAt = data.remove("\$createdAt")?.let { it as Long }
        this.updatedAt = data.remove("\$updatedAt")?.let { it as Long }
        this.revision = rawStateTransition.remove("\$revision") as Int

        this.data = data

    }

    override fun toJSON(): Map<String, Any> {
        var json = super.toJSON() as MutableMap<String, Any>
        json["\$id"] = id
        json["\$type"] = documentType
        json["\$revision"] = revision

        data.keys.iterator().forEach {
            data[it]?.let { it1 -> json[it] = it1 }
        }

        updatedAt?.let { json["\$updatedAt"] = it }

        return json
    }
}