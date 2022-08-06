/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashj.platform.dpp.identity

import com.google.common.base.Preconditions
import org.dashj.platform.dpp.BaseObject
import org.dashj.platform.dpp.Metadata
import org.dashj.platform.dpp.identifier.Identifier
import kotlin.math.max

class Identity(
    var id: Identifier,
    var balance: Long,
    val publicKeys: MutableList<IdentityPublicKey>,
    val revision: Int,
    protocolVersion: Int
) : BaseObject(protocolVersion) {

    var assetLockProof: AssetLockProof? = null
    var metadata: Metadata? = null

    constructor(rawIdentity: Map<String, Any?>) : this(
        Identifier.from(rawIdentity["id"]),
        rawIdentity["balance"].toString().toLong(),
        (rawIdentity["publicKeys"] as List<Any>).map { IdentityPublicKey(it as Map<String, Any>) }.toMutableList(),
        rawIdentity["revision"] as Int,
        rawIdentity["protocolVersion"] as Int
    )

    constructor(id: Identifier, publicKeys: List<IdentityPublicKey>, revision: Int, protocolVersion: Int) :
        this(id, 0, publicKeys.toMutableList(), revision, protocolVersion)

    fun getPublicKeyById(keyId: Int): IdentityPublicKey? {
        Preconditions.checkArgument(keyId >= 0, "keyId ($keyId) must be >= 0")
        return publicKeys.find { it.id == keyId }
    }

    override fun toObject(): Map<String, Any> {
        return mapOf(
            "protocolVersion" to protocolVersion,
            "id" to id,
            "publicKeys" to publicKeys.map { it.toObject() },
            "balance" to balance,
            "revision" to revision
        )
    }

    override fun toJSON(): Map<String, Any> {
        return mapOf(
            "protocolVersion" to protocolVersion,
            "id" to id,
            "publicKeys" to publicKeys.map { it.toJSON() },
            "balance" to balance,
            "revision" to revision
        )
    }

    fun increaseBalance(amount: Long): Long {
        balance += amount
        return balance
    }

    fun reduceBalance(amount: Long): Long {
        balance -= amount
        return balance
    }

    /**
     * Get the biggest public key ID
     * @returns {number}
     */
    fun getPublicKeyMaxId(): Int {
        return publicKeys.fold(-1) { result, publicKey ->
            max(publicKey.id, result)
        }
    }
}
