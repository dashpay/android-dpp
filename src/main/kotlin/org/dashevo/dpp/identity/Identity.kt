/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashevo.dpp.identity

import com.google.common.base.Preconditions
import org.bitcoinj.core.TransactionOutPoint
import org.dashevo.dpp.BaseObject

class Identity(var id: String,
               var balance: Long,
               var publicKeys: List<IdentityPublicKey>,
               val revision: Int,
               val protocolVersion: Int) : BaseObject() {

    companion object {
        const val PROTOCOL_VERSION: Int = 0
    }

    var lockedOutpoint: TransactionOutPoint? = null

    constructor(rawIdentity: Map<String, Any?>) : this(rawIdentity["id"] as String,
            rawIdentity["balance"].toString().toLong(),
            (rawIdentity["publicKeys"] as List<Any>).map { IdentityPublicKey(it as Map<String, Any>) },
            rawIdentity["revision"] as Int,
            rawIdentity["protocolVersion"] as Int)

    constructor(id: String, publicKeys: List<IdentityPublicKey>, revision: Int, protocolVersion: Int)
            : this(id, 0, publicKeys, revision, protocolVersion)

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

    fun decreaseBalance(amount: Long): Long {
        balance -= amount
        return balance
    }
}