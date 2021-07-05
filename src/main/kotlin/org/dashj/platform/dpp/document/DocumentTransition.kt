/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashevo.dpp.document

import org.dashevo.dpp.identifier.Identifier

abstract class DocumentTransition {

    enum class Action(val value: Int) {
        CREATE(0),
        REPLACE(1),
        UPDATE(2),
        DELETE(3);

        companion object {
            private val values = values()
            fun getByCode(code: Int): Action {
                return values.filter { it.ordinal == code }[0]
            }

            fun getByName(name: String): Action {
                return values.filter { it.name.toLowerCase() == name }[0]
            }

            fun getValidNames(): List<String> {
                return values.map { it.name.toLowerCase() }
            }
        }
    }

    abstract val action: Action
    val id: Identifier
    val type: String
    var dataContractId: Identifier

    constructor(rawStateTransition: MutableMap<String, Any?>) {
        type = rawStateTransition["\$type"] as String
        id = Identifier.from(rawStateTransition["\$id"])
        dataContractId = Identifier.from(rawStateTransition["\$dataContractId"]!!)
    }

    fun toObject(): Map<String, Any?> {
        return toObject(false)
    }

    open fun toObject(skipIdentifierConversion: Boolean): MutableMap<String, Any?> {
        val map = hashMapOf<String, Any?>(
                "\$id" to id,
                "\$type" to type,
                "\$action" to action.value,
                "\$dataContractId" to dataContractId
        )

        if (!skipIdentifierConversion) {
            map["\$id"] = id.toBuffer()
            map["\$dataContractId"] = dataContractId.toBuffer()
        }

        return map
    }

    open fun toJSON(): Map<String, Any?> {
        val json = toObject(true)
        json["\$id"] = id.toString()
        json["\$dataContractId"] = dataContractId.toString()
        return json
    }
}