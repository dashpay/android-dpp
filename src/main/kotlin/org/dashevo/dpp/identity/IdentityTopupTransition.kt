/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashevo.dpp.identity

import org.dashevo.dpp.identifier.Identifier
import org.dashevo.dpp.toBase64

class IdentityTopupTransition : IdentityStateTransition {

    val identityId: Identifier
    var lockedOutPoint: ByteArray  // base64

    constructor(identityId: Identifier,
                lockedOutPoint: ByteArray,
                 protocolVersion: Int = 0)
    : super(Types.IDENTITY_TOP_UP, protocolVersion) {
        this.lockedOutPoint = lockedOutPoint
        this.identityId = identityId
    }

    constructor(rawStateTransition: MutableMap<String, Any?>)
            : super(rawStateTransition) {
        lockedOutPoint = rawStateTransition["lockedOutPoint"] as ByteArray
        identityId = Identifier.from(rawStateTransition["identityId"]!!)
    }

    override fun toObject(skipSignature: Boolean, skipIdentifiersConversion: Boolean): MutableMap<String, Any?> {
        val map = super.toObject(skipSignature, skipIdentifiersConversion)
        map["identityId"] = identityId
        map["lockedOutPoint"] = lockedOutPoint

        if (!skipIdentifiersConversion) {
            map["identityId"] = identityId.toBuffer()
        }

        return map
    }

    override fun toJSON(skipSignature: Boolean): MutableMap<String, Any?> {
        var json = super.toJSON(skipSignature)
        json["lockedOutPoint"] = lockedOutPoint.toBase64()
        json["identityId"] = identityId.toString()
        return json
    }

    fun getOwnerId() : Identifier {
        return identityId
    }
}