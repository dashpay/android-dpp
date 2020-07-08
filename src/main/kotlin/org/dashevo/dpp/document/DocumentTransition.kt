/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashevo.dpp.document

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
    var dataContractId: String

    constructor(rawStateTransition: MutableMap<String, Any?>) {
        dataContractId = rawStateTransition["\$dataContractId"] as String
    }

    open fun toJSON(): Map<String, Any> {
        var json = HashMap<String, Any>()
        json["\$action"] = action.value
        json["\$dataContractId"] = dataContractId
        return json
    }
}