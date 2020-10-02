/**
 * Copyright (c) 2018-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashevo.dpp.document

import org.dashevo.dpp.BaseObject
import java.time.Instant
import java.util.*
import kotlin.collections.HashMap

class Document(rawDocument: MutableMap<String, Any?>) : BaseObject() {

    companion object {
        const val PROTOCOL_VERSION = 0
    }

    var id: String
    var type: String
    var dataContractId: String
    var ownerId: String
    lateinit var entropy: String
    var revision: Int = 0
    var data: Map<String, Any?>
    var createdAt: Long?
    var updatedAt: Long?
    var protocolVersion: Int

    init {
        val data = HashMap(rawDocument)

        this.id = data.remove("\$id") as String
        this.type = data.remove("\$type") as String
        this.dataContractId = data.remove("\$dataContractId") as String
        this.ownerId = data.remove("\$ownerId") as String
        this.revision = data.remove("\$revision") as Int
        this.createdAt = data.remove("\$createdAt")?.let { it as Long }
        this.updatedAt = data.remove("\$updatedAt")?.let { it as Long }
        this.protocolVersion = data.remove("\$protocolVersion") as Int

        this.data = data
    }

    override fun toJSON(): Map<String, Any> {
        val json = hashMapOf(
            "\$protocolVersion" to protocolVersion,
            "\$id" to id,
            "\$type" to type,
            "\$dataContractId" to dataContractId,
            "\$ownerId" to ownerId,
            "\$revision" to revision
        )

        data.keys.iterator().forEach {
            data[it]?.let { it1 -> json[it] = it1 }
        }

        createdAt?.let { json["\$createdAt"] = it }
        updatedAt?.let { json["\$updatedAt"] = it }

        return json
    }

    fun get(path: String): Any {
        TODO("get field specified by path")
    }

    fun set(path: String, value: Any) {
        TODO("set field specified by path to the value")
    }
}

