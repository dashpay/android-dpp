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
import org.dashevo.dpp.toBase64
import org.dashevo.dpp.util.HashUtils.byteArrayfromBase64orByteArray
import org.dashevo.dpp.util.HashUtils.fromBase64

class IdentityPublicKey(val id: Int,
                        val type: TYPES,
                        val data: ByteArray) : BaseObject() {

    enum class TYPES(val value: Int) {
        ECDSA_SECP256K1(0),
        BLS12_381(1);

        companion object {
            private val values = values()
            fun getByCode(code: Int): TYPES {
                return values.filter { it.value == code }[0]
            }
        }
    }

    constructor(id: Int, type: TYPES, data: String) : this(id, type, fromBase64(data))

    constructor(rawIdentityPublicKey: Map<String, Any>) :
            this(rawIdentityPublicKey["id"] as Int,
                    TYPES.getByCode(rawIdentityPublicKey["type"] as Int),
                    byteArrayfromBase64orByteArray(rawIdentityPublicKey["data"] ?: error("data is missing")))

    override fun toObject(): Map<String, Any> {
        return hashMapOf<String, Any>(
                "id" to id,
                "type" to type.value,
                "data" to data
        )
    }

    override fun toJSON(): Map<String, Any> {
        return hashMapOf(
                "id" to id,
                "type" to type.value,
                "data" to data.toBase64()
        )
    }

    override fun equals(other: Any?): Boolean {
        if (other !is IdentityPublicKey)
            return false
        return other.id == id &&
                other.type == type &&
                other.data.contentEquals(data)
    }

    override fun hashCode(): Int {
        val hash = hash()
        return Utils.readUint32(hash, 0).toInt()
    }

    fun getKey(): ECKey {
        return ECKey.fromPublicOnly(data)
    }

    override fun hash(): ByteArray {
        if (data.isEmpty()) {
            throw EmptyPublicKeyDataException()
        }
        return super.hash()
    }
}