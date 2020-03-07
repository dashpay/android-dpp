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
import java.nio.charset.Charset

class Document(rawDocument: MutableMap<String, Any>) : BaseObject() {

    enum class Action (val action: Int) {
        CREATE(1),
        REPLACE(2),
        UPDATE(2), //Keeping for backward compatibility
        DELETE(4);

        companion object {
            fun getByCode(code: Int): Action {
                return Action.values().filter { it.ordinal == code }[0]
            }
        }
    }

    companion object DEFAULTS {
        const val REVISION = 1
        val ACTION = Action.CREATE
        val SYSTEM_PREFIX = '$'
    }

    var id: String = ""
       get() {
           if (field.isEmpty()) {
               val utf8charset = Charset.forName("UTF-8")
               val scopeHash = HashUtils.toHash((contractId + userId + type + entropy).toByteArray(utf8charset))
               field = Base58.encode(scopeHash)
           }
           return field
       }
    lateinit var type: String
    lateinit var contractId: String
    lateinit var userId: String
    lateinit var entropy: String
    private var rev: Int = 0
    var data: Map<String, Any>
    var action: Action = Action.CREATE
        set(value) {
            if (Action.DELETE == value && this.data.keys.isNotEmpty()) {
                throw IllegalStateException("Data is not allowed when deleting a document.")
            }
            field = value
        }

    init {
        val data = HashMap<String, Any>(rawDocument)

        if (rawDocument.containsKey("\$type")) {
            this.type = rawDocument["\$type"] as String
            rawDocument.remove("\$type")
        }
        if (rawDocument.containsKey("\$contractId")) {
            this.contractId = rawDocument["\$contractId"] as String
            rawDocument.remove("\$contractId")
        }
        if (rawDocument.containsKey("\$userId")) {
            this.userId = rawDocument["\$userId"] as String
            rawDocument.remove("\$userId")
        }
        if (rawDocument.containsKey("\$entropy")) {
            this.entropy = rawDocument["\$entropy"] as String
            rawDocument.remove("\$entropy")
        }
        if (rawDocument.containsKey("\$rev")) {
            this.rev = rawDocument["\$rev"] as Int
            rawDocument.remove("\$rev")
        }

        this.data = data
    }

    override fun toJSON(): Map<String, Any> {
        val json = hashMapOf<String, Any>()
        json.put("\$type", type)
        json.put("\$userId", userId)
        json.put("\$contractId", contractId)
        json.put("\$entropy", contractId)
        json.put("\$rev", rev)

        data.keys.iterator().forEach {
            data.get(it)?.let { it1 -> json.put(it, it1) }
        }

        return json
    }

}

