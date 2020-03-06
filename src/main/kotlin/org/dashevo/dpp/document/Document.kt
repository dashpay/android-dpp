/**
 * Copyright (c) 2018-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashevo.dpp.document

import org.bitcoinj.core.Base58
import org.dashevo.dpp.util.HashUtils
import java.nio.charset.Charset

class Document(rawDocument: MutableMap<String, Any>) {

    enum class Action {
        CREATE,
        UPDATE,
        DELETE;

        companion object {
            fun getByCode(code: Int): Action {
                return Action.values().filter { it.ordinal == code }[0]
            }
        }
    }

    companion object DEFAULTS {
        const val REVISION = 1
        val ACTION = Action.CREATE
    }

    var id: String = ""
       get() {
           if (field.isEmpty()) {
               val utf8charset = Charset.forName("UTF-8")
               val scopeHash = HashUtils.toHash((scope + scopeId).toByteArray(utf8charset))
               field = Base58.encode(scopeHash)
           }
           return field
       }
    lateinit var type: String
    lateinit var scopeId: String
    lateinit var scope: String
    private var rev: Int = 0
    lateinit var meta: Map<String, Any>
    var data: Map<String, Any>
    var metadata: Map<String, Any>? = mapOf()
    var action: Action = Action.CREATE
        set(value) {
            if (Action.DELETE == value && !this.data.keys.isEmpty()) {
                throw IllegalStateException("Data is not allowed when deleting a document.")
            }
            field = value
        }

    init {
        val data = HashMap<String, Any>(rawDocument)

        if (rawDocument.containsKey("\$type")) {
            this.type = rawDocument.get("\$type") as String
            rawDocument.remove("\$type")
        }
        if (rawDocument.containsKey("\$scopeId")) {
            this.scopeId = rawDocument.get("\$scopeId") as String
            rawDocument.remove("\$scopeId")
        }
        if (rawDocument.containsKey("\$scope")) {
            this.scope = rawDocument.get("\$scope").toString()
            rawDocument.remove("\$scope")
        }
        if (rawDocument.containsKey("\$action")) {
            val code = rawDocument.get("\$action") as Int
            this.action = Action.getByCode(code)
            rawDocument.remove("\$action")
        }
        if (rawDocument.containsKey("\$rev")) {
            this.rev = rawDocument.get("\$rev") as Int
            rawDocument.remove("\$rev")
        }
        if (rawDocument.containsKey("\$meta")) {
            this.meta = rawDocument.get("\$meta") as Map<String, Any>
            rawDocument.remove("\$meta")
        }

        this.data = data
    }

    fun removeMetadata() {
        this.metadata = null
    }

    fun toJSON(): Map<String, Any> {
        val json = hashMapOf<String, Any>()
        json.put("\$type", type)
        json.put("\$scope", scope)
        json.put("\$scopeId", scopeId)
        json.put("\$rev", rev)
        json.put("\$action", action)

        data.keys.iterator().forEach {
            data.get(it)?.let { it1 -> json.put(it, it1) }
        }

        if (json.containsKey("\$meta")){
            json.remove("\$meta")
        }

        return json
    }

    fun serialize(skipMeta: Boolean = false): ByteArray {
        val json = this.toJSON().toMutableMap()

        if (skipMeta) {
            json.remove("\$meta")
        }

        return HashUtils.encode(json)
    }

    fun hash(): ByteArray {
        return HashUtils.toHash(this.toJSON())
    }

}

