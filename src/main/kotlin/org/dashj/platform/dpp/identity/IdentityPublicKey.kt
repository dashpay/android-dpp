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
import org.dashj.platform.dpp.errors.concensus.signature.InvalidIdentityPublicKeyTypeException
import org.dashj.platform.dpp.identity.errors.EmptyPublicKeyDataException
import org.dashj.platform.dpp.toBase64
import org.dashj.platform.dpp.util.Converters

class IdentityPublicKey(
    val id: Int,
    val type: Type,
    val purpose: Purpose,
    val securityLevel: SecurityLevel,
    val data: ByteArray,
    val readOnly: Boolean,
    var disabledAt: Long? = null,
    var signature: ByteArray? = null
) : BaseObject() {

    enum class Type(val value: Int) {
        ECDSA_SECP256K1(0),
        BLS12_381(1),
        ECDSA_HASH160(2),
        BIP13_SCRIPT_HASH(3),
        EDSSA_25519_HASH160(4),
        INVALID(30000); // for tests

        companion object {
            private val values = values()
            fun getByCode(code: Int): Type {
                return values.filter { it.value == code }[0]
            }
        }
    }

    enum class Purpose(val value: Int) {
        AUTHENTICATION(0),
        ENCRYPTION(1),
        DECRYPTION(2),
        WITHDRAW(3),
        SYSTEM(4),
        VOTING(5);

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
            ),
            Purpose.WITHDRAW to listOf(
                SecurityLevel.CRITICAL
            )
        )
    }

    constructor(id: Int, type: Type, purpose: Purpose, securityLevel: SecurityLevel, data: String, readOnly: Boolean) :
        this(id, type, purpose, securityLevel, Converters.fromBase64(data), readOnly)

    constructor(id: Int, type: Type, data: String) :
        this(id, type, Purpose.AUTHENTICATION, SecurityLevel.MASTER, Converters.fromBase64(data), true)

    constructor(id: Int, type: Type, data: ByteArray) :
        this(id, type, Purpose.AUTHENTICATION, SecurityLevel.MASTER, data, true)

    constructor(rawIdentityPublicKey: Map<String, Any?>) :
        this(
            rawIdentityPublicKey["id"] as Int,
            when (rawIdentityPublicKey["type"]) {
                is Type -> rawIdentityPublicKey["type"] as Type
                is Int -> Type.getByCode(rawIdentityPublicKey["type"] as Int)
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
                else -> SecurityLevel.CRITICAL
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
        ) {
            when (rawIdentityPublicKey["signature"]) {
                is String -> signature = Converters.fromBase64(rawIdentityPublicKey["signature"] as String)
                is ByteArray -> signature = rawIdentityPublicKey["signature"] as ByteArray
                else -> {}
            }
        }

    override fun toObject(): Map<String, Any> {
        return toObject(false)
    }

    fun toObject(skipSignature: Boolean): Map<String, Any> {
        val objMap = hashMapOf<String, Any>(
            "id" to id,
            "type" to type.value,
            "purpose" to purpose.value,
            "securityLevel" to securityLevel.value,
            "data" to data,
            "readOnly" to readOnly
        )
        disabledAt?.run {
            objMap.put("disabledAt", this)
        }
        if (signature != null && !skipSignature) {
            objMap["signature"] = signature!!
        }
        return objMap
    }

    override fun toJSON(): Map<String, Any> {
        val json = toObject().toMutableMap()
        json["data"] = data.toBase64()
        signature?.run {
            json["signature"] = toBase64()
        }
        return json
    }

    fun isMaster(): Boolean {
        return securityLevel == IdentityPublicKey.SecurityLevel.MASTER
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
        return when (type) {
            Type.ECDSA_HASH160, Type.BIP13_SCRIPT_HASH -> return data
            Type.BLS12_381, Type.ECDSA_SECP256K1 -> Utils.sha256hash160(data)
            else -> throw InvalidIdentityPublicKeyTypeException(type)
        }
    }

    override fun toString(): String {
        return toJSON().toString()
    }

    fun copy(skipSignature: Boolean): IdentityPublicKey {
        return IdentityPublicKey(id, type, purpose, securityLevel, data, readOnly, disabledAt, if (skipSignature) null else signature)
    }
}
