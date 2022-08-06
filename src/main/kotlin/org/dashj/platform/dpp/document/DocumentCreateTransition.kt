/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashj.platform.dpp.document

import org.bitcoinj.core.Base58
import org.dashj.platform.dpp.contract.DataContract
import java.lang.IllegalStateException

open class DocumentCreateTransition(rawStateTransition: MutableMap<String, Any?>, dataContract: DataContract) :
    DataDocumentTransition(rawStateTransition, dataContract) {

    companion object {
        const val INITIAL_REVISION = 1
    }

    override val action = Action.CREATE
    var entropy: ByteArray
    var createdAt: Long?
    var updatedAt: Long?

    init {
        val entropy = rawStateTransition["\$entropy"]
        this.entropy = when (entropy) {
            is ByteArray -> entropy
            is String -> Base58.decode(entropy)
            else -> throw IllegalStateException("entropy is not a ByteArray or String")
        }
        this.createdAt = rawStateTransition["\$createdAt"]?.let { it as Long }
        this.updatedAt = rawStateTransition["\$updatedAt"]?.let { it as Long }
    }

    override fun toObject(skipIdentifierConversion: Boolean): MutableMap<String, Any?> {
        val map = super.toObject(skipIdentifierConversion)

        map["\$entropy"] = entropy

        createdAt?.let { map["\$createdAt"] = it }
        updatedAt?.let { map["\$updatedAt"] = it }

        return map
    }
}
