/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashj.platform.dpp.identity

import org.bitcoinj.core.ECKey
import org.bitcoinj.core.Utils
import org.dashj.platform.dpp.BaseObject
import org.dashj.platform.dpp.identity.errors.EmptyPublicKeyDataException
import org.dashj.platform.dpp.toBase64
import org.dashj.platform.dpp.util.Converters

class IdentityPublicKey(
    val id: Int,
    val type: TYPES,
    val purpose: Purpose,
    val securityLevel: SecurityLevel,
    val data: ByteArray,
    val readOnly: Boolean
) : BaseObject() {

    enum class TYPES(val value: Int) {
        ECDSA_SECP256K1(0),
        BLS12_381(1),
        ECDSA_HASH160(2);

        companion object {
            private val values = values()
            fun getByCode(code: Int): TYPES {
                return values.filter { it.value == code }[0]
            }
        }
    }

    enum class Purpose(val value: Int) {
        AUTHENTICATION(0),
        ENCRYPTION(1),
        DECRYPTION(2);

        companion object {
            private val values = values()
            fun getByCode(code: Int): Purpose {
                return values.filter { it.value == code }[0]
            }
        }
    }

    enum class SecurityLevel(val value: Int) {
        MASTER(0),
        CRITICAL(1),
        HIGH(2),
        MEDIUM(3);

        companion object {
            private val values = values()
            fun getByCode(code: Int): SecurityLevel {
                return values.filter { it.value == code }[0]
            }
        }
    }

    companion object {
        val allowedSecurityLevels = mapOf(
            Purpose.AUTHENTICATION to listOf(
                SecurityLevel.MASTER,
                SecurityLevel.CRITICAL,
                SecurityLevel.HIGH,
                SecurityLevel.MEDIUM
            ),
            Purpose.DECRYPTION to listOf(
                SecurityLevel.MEDIUM
            ),
            Purpose.ENCRYPTION to listOf(
                SecurityLevel.MEDIUM
            )
        )
    }

    constructor(id: Int, type: TYPES, purpose: Purpose, securityLevel: SecurityLevel, data: String, readOnly: Boolean) :
        this(id, type, purpose, securityLevel, Converters.fromBase64(data), readOnly)

    constructor(id: Int, type: TYPES, data: String) :
        this(id, type, Purpose.AUTHENTICATION, SecurityLevel.MASTER, Converters.fromBase64(data), true)

    constructor(id: Int, type: TYPES, data: ByteArray) :
        this(id, type, Purpose.AUTHENTICATION, SecurityLevel.MASTER, data, true)

    constructor(rawIdentityPublicKey: Map<String, Any>) :
        this(
            rawIdentityPublicKey["id"] as Int,
            when (rawIdentityPublicKey["type"]) {
                is TYPES -> rawIdentityPublicKey["type"] as TYPES
                is Int -> TYPES.getByCode(rawIdentityPublicKey["type"] as Int)
                else -> error("invalid type")
            },
            when (rawIdentityPublicKey["purpose"]) {
                is Purpose -> rawIdentityPublicKey["purpose"] as Purpose
                is Int -> Purpose.getByCode(rawIdentityPublicKey["purpose"] as Int)
                else -> Purpose.AUTHENTICATION
            },
            when (rawIdentityPublicKey["securityLevel"]) {
                is SecurityLevel -> rawIdentityPublicKey["securityLevel"] as SecurityLevel
                is Int -> SecurityLevel.getByCode(rawIdentityPublicKey["securityLevel"] as Int)
                else -> SecurityLevel.MASTER
            },
            when (rawIdentityPublicKey["data"]) {
                is String -> Converters.fromBase64(rawIdentityPublicKey["data"] as String)
                is ByteArray -> rawIdentityPublicKey["data"] as ByteArray
                else -> ByteArray(0)
            },
            when (rawIdentityPublicKey["readOnly"]) {
                is Boolean -> rawIdentityPublicKey["readOnly"] as Boolean
                else -> false
            }
        )

    override fun toObject(): Map<String, Any> {
        return hashMapOf<String, Any>(
            "id" to id,
            "type" to type.value,
            "purpose" to purpose.value,
            "securityLevel" to securityLevel.value,
            "data" to data,
            "readOnly" to readOnly
        )
    }

    override fun toJSON(): Map<String, Any> {
        return hashMapOf(
            "id" to id,
            "type" to type.value,
            "purpose" to purpose.value,
            "securityLevel" to securityLevel.value,
            "data" to data.toBase64(),
            "readOnly" to readOnly
        )
    }

    override fun equals(other: Any?): Boolean {
        if (other !is IdentityPublicKey) {
            return false
        }
        return other.id == id &&
            other.type == type &&
            other.purpose == purpose &&
            other.securityLevel == securityLevel &&
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
        if (type == TYPES.ECDSA_HASH160) {
            return data
        }
        return super.hash()
    }

    override fun toString(): String {
        return toJSON().toString()
    }
}
