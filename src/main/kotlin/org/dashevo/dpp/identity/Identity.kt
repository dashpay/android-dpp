/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashevo.dpp.identity

import org.dashevo.dpp.BaseObject

class Identity(var id: String,
               var type: IdentityType,
               var publicKeys: List<IdentityPublicKey>) : BaseObject() {

    companion object {
        val MAX_RESERVED_TYPE = 32767
    }

    constructor(rawIdentity: Map<String, Any>) : this(rawIdentity["id"] as String,
            IdentityType.getByCode(rawIdentity["type"] as Int),
            (rawIdentity["publicKeys"] as List<Any>).map { IdentityPublicKey(it as Map<String, Any>) })

    enum class IdentityType (val value: Int) {
        USER(1),
        APPLICATION(2);

        companion object {
            private val values = values()
            fun getByCode(code: Int): IdentityType {
                return values.filter { it.value == code }[0]
            }
        }
    }

    fun findPublicKeyById(keyId: Int) : IdentityPublicKey? {
        return publicKeys.find { it.id == keyId }
    }

    override fun toJSON(): Map<String, Any> {
        val json = hashMapOf<String, Any>()
        json["id"] = id
        json["type"] = type.value
        json["publicKeys"] = publicKeys

        return json
    }

}