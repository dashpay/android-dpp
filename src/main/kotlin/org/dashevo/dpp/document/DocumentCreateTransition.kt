/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashevo.dpp.document

import java.time.Instant
import java.util.*
import kotlin.collections.HashMap


open class DocumentCreateTransition : DocumentTransition {

    companion object {
        val INITIAL_REVISION = 1
    }

    override val action = Action.CREATE
    var id: String
    var documentType: String
    var entropy: String
    var data: Map<String, Any?>
    var createdAt: Date?
    var updatedAt: Date?

    constructor(rawStateTransition: MutableMap<String, Any?>) : super(rawStateTransition) {
        val data = HashMap(rawStateTransition)

        this.id = data.remove("\$id") as String
        this.documentType = data.remove("\$type") as String
        this.entropy = data.remove("\$entropy") as String
        data.remove("\$action")
        data.remove("\$dataContractId") as String
        this.createdAt = data.remove("\$createdAt")?.let { Date.from(Instant.parse(it as String)) }
        this.updatedAt = data.remove("\$updatedAt")?.let { Date.from(Instant.parse(it as String)) }

        this.data = data
    }

    override fun toJSON(): Map<String, Any> {
        var json = super.toJSON() as MutableMap<String, Any>
        json["\$id"] = id
        json["\$type"] = documentType
        json["\$entropy"] = entropy

        data.keys.iterator().forEach {
            data[it]?.let { it1 -> json[it] = it1 }
        }

        createdAt?.let { json["\$createdAt"] = it.toInstant().toString() }
        updatedAt?.let { json["\$updatedAt"] = it.toInstant().toString() }

        return json
    }
}