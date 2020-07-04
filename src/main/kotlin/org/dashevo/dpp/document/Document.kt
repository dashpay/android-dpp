/**
 * Copyright (c) 2018-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashevo.dpp.document

import org.bitcoinj.core.Base58
import org.dashevo.dpp.BaseObject
import org.dashevo.dpp.util.HashUtils
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset

class Document(rawDocument: MutableMap<String, Any?>) : BaseObject() {

    enum class Action (val value: Int) {
        CREATE(1),
        REPLACE(2),
        UPDATE(2), //Keeping for backward compatibility
        DELETE(4);

        companion object {
            private val values = values()
            fun getByCode(code: Int): Action {
                return values.filter { it.ordinal == code }[0]
            }
        }
    }

    companion object DEFAULTS {
        const val REVISION = 1
        val ACTION = Action.CREATE
        val SYSTEM_PREFIX = '$'
    }

    var id: String
    var type: String
    var dataContractId: String
    var ownerId: String
    lateinit var entropy: String
    var revision: Int = 0
    var data: Map<String, Any?>
    var action: Action = Action.CREATE
        set(value) {
            if (Action.DELETE == value && this.data.keys.isNotEmpty()) {
                throw IllegalStateException("Data is not allowed when deleting a document.")
            }
            field = value
        }

    init {
        val data = HashMap(rawDocument)

        this.id = data.remove("\$id") as String
        this.type = data.remove("\$type") as String
        this.dataContractId = data.remove("\$dataContractId") as String
        this.ownerId = data.remove("\$ownerId") as String
        this.revision = data.remove("\$revision") as Int

        this.data = data
    }

    override fun toJSON(): Map<String, Any> {
        val json = hashMapOf<String, Any>()
        json["\$id"] = id
        json["\$type"] = type
        json["\$dataContractId"] = dataContractId
        json["\$ownerId"] = ownerId
        json["\$revision"] = revision

        data.keys.iterator().forEach {
            data[it]?.let { it1 -> json[it] = it1 }
        }

        return json
    }

    fun get(path: String): Any {
        TODO("get field specified by path")
    }

    fun set(path: String, value: Any) {
        TODO("set field specified by path to the value")
    }
}

