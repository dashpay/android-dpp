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
        val data = HashMap(rawDocument)

        this.type = rawDocument.remove("\$type") as String
        this.contractId = rawDocument.remove("\$contractId") as String
        this.userId = rawDocument.remove("\$userId") as String
        this.entropy = rawDocument.remove("\$entropy") as String
        this.rev = rawDocument.remove("\$rev") as Int

        this.data = data
    }

    override fun toJSON(): Map<String, Any> {
        val json = hashMapOf<String, Any>()
        json["\$type"] = type
        json["\$userId"] = userId
        json["\$contractId"] = contractId
        json["\$entropy"] = contractId
        json["\$rev"] = rev

        data.keys.iterator().forEach {
            data[it]?.let { it1 -> json[it] = it1 }
        }

        return json
    }

}

