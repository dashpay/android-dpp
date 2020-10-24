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
               var publicKeys: List<IdentityPublicKey>) : BaseObject() {

    var lockedOutpoint: TransactionOutPoint? = null

    constructor(rawIdentity: Map<String, Any?>) : this(rawIdentity["id"] as String,
            rawIdentity["balance"].toString().toLong(),
            (rawIdentity["publicKeys"] as List<Any>).map { IdentityPublicKey(it as Map<String, Any>) })

    constructor(id: String, publicKeys: List<IdentityPublicKey>) : this(id, 0, publicKeys)

    fun getPublicKeyById(keyId: Int) : IdentityPublicKey? {
        Preconditions.checkArgument(keyId >= 0, "keyId ($keyId) must be >= 0")
        return publicKeys.find { it.id == keyId }
    }

    override fun toJSON(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "publicKeys" to publicKeys.map { it.toJSON() },
            "balance" to balance
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