/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashevo.dpp.identity

import org.dashevo.dpp.toBase64
import org.dashevo.dpp.util.HashUtils

class IdentityCreateTransition(var identityType: Identity.IdentityType?,
                               var lockedOutPoint: String?,
                               var publicKeys: List<IdentityPublicKey>,
                               protocolVersion: Int = 0)
    : IdentityStateTransition(Types.IDENTITY_CREATE, protocolVersion) {

    var identityId: String

    init {
        identityId = HashUtils.toHash(HashUtils.fromBase64(lockedOutPoint!!)).toBase64()
    }

    constructor(rawStateTransition: MutableMap<String, Any?>)
            : this(rawStateTransition["identityType"] as? Identity.IdentityType,
            rawStateTransition["lockedOutPoint"] as String,
            (rawStateTransition["publicKeys"] as List<Any>).map { entry -> IdentityPublicKey(entry as MutableMap<String, Any>) }, 0) {
        identityId = HashUtils.toHash(HashUtils.fromBase64(lockedOutPoint!!)).toBase64()
    }

    override fun toJSON(skipSignature: Boolean): Map<String, Any> {
        var json = super.toJSON() as MutableMap<String, Any>
        json["identityType"] = identityType as String
        json["lockedOutPoint"] = lockedOutPoint as String
        json["publicKeys"] = publicKeys.map { it.toJSON() }
        return json
    }
}