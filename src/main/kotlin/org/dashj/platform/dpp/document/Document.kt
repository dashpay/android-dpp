/**
 * Copyright (c) 2018-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashj.platform.dpp.document

import org.dashj.platform.dpp.BaseObject
import org.dashj.platform.dpp.Metadata
import org.dashj.platform.dpp.ProtocolVersion
import org.dashj.platform.dpp.contract.DataContract
import org.dashj.platform.dpp.deepCopy
import org.dashj.platform.dpp.identifier.Identifier
import org.dashj.platform.dpp.util.Converters
import kotlin.collections.HashMap

class Document(rawDocument: Map<String, Any?>, dataContract: DataContract) : BaseObject() {

    val dataContract: DataContract
    var id: Identifier
    var type: String
    var dataContractId: Identifier
    var ownerId: Identifier
    lateinit var entropy: ByteArray
    var revision: Int = 0
    var data: Map<String, Any?>
    var createdAt: Long?
    var updatedAt: Long?
    var metadata: Metadata? = null

    init {
        this.dataContract = dataContract
        val data = HashMap(rawDocument)

        this.id = Identifier.from(data.remove("\$id")!!)
        this.type = data.remove("\$type") as String
        this.dataContractId = Identifier.from(data.remove("\$dataContractId")!!)
        this.ownerId = Identifier.from(data.remove("\$ownerId")!!)
        this.revision = if (data.containsKey("\$revision")) {
            data.remove("\$revision") as Int
        } else {
            DocumentCreateTransition.INITIAL_REVISION
        }
        this.createdAt = data.remove("\$createdAt")?.let { it as Long }
        this.updatedAt = data.remove("\$updatedAt")?.let { it as Long }
        this.protocolVersion = if (data.containsKey("\$protocolVersion")) {
            data.remove("\$protocolVersion") as Int
        } else {
            ProtocolVersion.latestVersion
        }

        this.data = data
    }

    override fun toObject(): Map<String, Any?> {
        return toObject(false)
    }

    fun toObject(skipIdentifierConversion: Boolean): MutableMap<String, Any?> {
        val map = hashMapOf<String, Any?>(
            "\$protocolVersion" to protocolVersion,
            "\$id" to id,
            "\$type" to type,
            "\$dataContractId" to dataContractId,
            "\$ownerId" to ownerId,
            "\$revision" to revision
        )

        val deepCopy = data.deepCopy()
        map.putAll(deepCopy)

        createdAt?.let { map["\$createdAt"] = it }
        updatedAt?.let { map["\$updatedAt"] = it }

        if (!skipIdentifierConversion) {
            map["\$id"] = id.toBuffer()
            map["\$dataContractId"] = dataContractId.toBuffer()
            map["\$ownerId"] = ownerId.toBuffer()

            // change binary items items in data to ByteArray
            Converters.convertIdentifierToByteArray(map)
        }

        return map
    }

    override fun toJSON(): Map<String, Any?> {
        val json = toObject(true)
        // change binary items in data to base64
        Converters.convertDataToString(json)
        return json
    }

    fun get(path: String): Any? {
        val keys = path.split("/")
        var value: Any? = data
        for (key in keys) {
            if ((value as Map<String, Any?>).containsKey(key)) {
                value = (value as Map<*, *>?)!![key]
            } else {
                return null
            }
        }
        return value
    }

    fun set(path: String, value: Any) {
        TODO("set field specified by path to the value")
    }

    fun setRevision(revision: Int): Document {
        this.revision = revision
        return this
    }
}
