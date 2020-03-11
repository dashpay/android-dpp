/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashevo.dpp.identity

class IdentityCreateTransition(var identityType: Identity.IdentityType?,
                               var lockedOutPoint: String?,
                               var publicKeys: List<IdentityPublicKey>,
                               protocolVersion: Int = 0)
    : IdentityStateTransition(Types.IDENTITY_CREATE, protocolVersion) {

    constructor(rawStateTransition: MutableMap<String, Any>)
            : this(rawStateTransition["identityType"] as? Identity.IdentityType,
            rawStateTransition["lockedOutPoint"] as String,
            (rawStateTransition["publicKeys"] as List<Any>).map { entry -> IdentityPublicKey(entry as MutableMap<String, Any>) }, 0)

    override fun toJSON(skipSignature: Boolean): Map<String, Any> {
        var json = super.toJSON() as MutableMap<String, Any>
        json["identityType"] = identityType as String
        json["lockedOutPoint"] = lockedOutPoint as String
        json["publicKeys"] = publicKeys.map {}
        return json
    }
}