/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashevo.dpp.identity

class IdentityTopupTransition : IdentityStateTransition {

    var identityId: String = "" // base58
    var lockedOutPoint: String  // base64

    constructor(lockedOutPoint: String,
                 identityId: String,
                 protocolVersion: Int = 0)
    : super(Types.IDENTITY_TOP_UP, protocolVersion) {
        this.lockedOutPoint = lockedOutPoint
        this.identityId = identityId
    }

    constructor(rawStateTransition: MutableMap<String, Any?>)
            : super(rawStateTransition) {
        lockedOutPoint = rawStateTransition["lockedOutPoint"] as String
        identityId = rawStateTransition["identityId"] as String
    }

    override fun toJSON(skipSignature: Boolean): MutableMap<String, Any?> {
        var json = super.toJSON(skipSignature)
        json["lockedOutPoint"] = lockedOutPoint
        json["identityId"] = identityId
        return json
    }

    fun getOwnerId() : String {
        return identityId
    }
}