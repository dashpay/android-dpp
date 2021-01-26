/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashevo.dpp.document

import org.bitcoinj.core.Base58
import java.lang.IllegalStateException


open class DocumentCreateTransition : DataDocumentTransition {

    companion object {
        const val INITIAL_REVISION = 1
    }

    override val action = Action.CREATE
    var entropy: ByteArray
    var createdAt: Long?
    var updatedAt: Long?

    constructor(rawStateTransition: MutableMap<String, Any?>) : super(rawStateTransition) {

        val entropy = rawStateTransition["\$entropy"]
        this.entropy = when (entropy) {
            is ByteArray -> entropy
            is String -> Base58.decode(entropy)
            else -> throw IllegalStateException("entropy is not a ByteArray or String")
        }

        this.createdAt = rawStateTransition["\$createdAt"]?.let { it as Long }
        this.updatedAt = rawStateTransition["\$updatedAt"]?.let { it as Long }
    }

    override fun toObject(skipIdentifierConversion: Boolean): MutableMap<String, Any> {
        val map = super.toObject(skipIdentifierConversion)

        map["\$entropy"] = entropy

        createdAt?.let { map["\$createdAt"] = it }
        updatedAt?.let { map["\$updatedAt"] = it }

        return map
    }

    //TODO: will this be covered by super?

    /*override fun toJSON(): Map<String, Any> {
        var json = super.toJSON() as MutableMap<String, Any>
        json["\$id"] = id
        json["\$type"] = documentType
        json["\$entropy"] = entropy


        createdAt?.let { json["\$createdAt"] = it }
        updatedAt?.let { json["\$updatedAt"] = it }

        return json
    }*/
}