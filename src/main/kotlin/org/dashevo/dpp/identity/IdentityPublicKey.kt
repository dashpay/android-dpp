/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashevo.dpp.identity

import org.bitcoinj.core.ECKey
import org.bitcoinj.core.Utils
import org.dashevo.dpp.BaseObject
import org.dashevo.dpp.identity.errors.EmptyPublicKeyDataException
import org.dashevo.dpp.util.HashUtils

class IdentityPublicKey(var id: Int,
                        var type: TYPES,
                        var data: String,
                        var isEnabled: Boolean) : BaseObject() {

    enum class TYPES(val value: Int) {
        ECDSA_SECP256K1(0),
        BLS(1);

        companion object {
            private val values = values()
            fun getByCode(code: Int): TYPES {
                return values.filter { it.value == code }[0]
            }
        }
    }

    constructor(rawIdentityPublicKey: Map<String, Any>) :
            this(rawIdentityPublicKey["id"] as Int,
                    TYPES.getByCode(rawIdentityPublicKey["type"] as Int),
                    rawIdentityPublicKey["data"] as String,
                    rawIdentityPublicKey["isEnabled"] as Boolean)


    override fun toJSON(): Map<String, Any> {
        val json = hashMapOf<String, Any>()
        json["id"] = id
        json["type"] = type.value
        json["data"] = data
        json["isEnabled"] = isEnabled
        return json
    }

    override fun equals(other: Any?): Boolean {
        if (other !is IdentityPublicKey)
            return false
        return other.id == id &&
                other.type == type &&
                other.data == data &&
                other.isEnabled == isEnabled
    }

    override fun hashCode(): Int {
        val hash = hashAsByteArray()
        return Utils.readUint32(hash, 0).toInt()
    }

    fun getKey(): ECKey {
        return ECKey.fromPublicOnly(HashUtils.fromBase64(data))
    }

    override fun hashAsByteArray(): ByteArray {
        if(data.isEmpty()) {
            throw EmptyPublicKeyDataException()
        }
        return super.hashAsByteArray()
    }

}