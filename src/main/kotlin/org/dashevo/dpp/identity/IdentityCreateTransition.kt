/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashevo.dpp.identity

import org.dashevo.dpp.toBase64
import org.dashevo.dpp.util.HashUtils

class IdentityCreateTransition : IdentityStateTransition {

    var identityType: Identity.IdentityType?
    var lockedOutPoint: String?
    var publicKeys: List<IdentityPublicKey>
    var identityId: String


    constructor(identityType: Identity.IdentityType?,
                 lockedOutPoint: String?,
                 publicKeys: List<IdentityPublicKey>,
                 protocolVersion: Int = 0)
    : super(Types.IDENTITY_CREATE, protocolVersion) {
        this.identityType = identityType
        this.lockedOutPoint = lockedOutPoint
        this.publicKeys = publicKeys
        identityId = HashUtils.toHash(HashUtils.fromBase64(lockedOutPoint!!)).toBase64()
    }

    constructor(rawStateTransition: MutableMap<String, Any?>)
            : super(rawStateTransition) {
        identityType = Identity.IdentityType.getByCode(rawStateTransition["identityType"] as Int)
        lockedOutPoint = rawStateTransition["lockedOutPoint"] as String
        publicKeys = (rawStateTransition["publicKeys"] as List<Any>).map { entry -> IdentityPublicKey(entry as MutableMap<String, Any>) }
        identityId = HashUtils.toHash(HashUtils.fromBase64(lockedOutPoint!!)).toBase64()
    }

    override fun toJSON(skipSignature: Boolean): Map<String, Any> {
        var json = super.toJSON(skipSignature) as MutableMap<String, Any>
        json["identityType"] = identityType!!.value
        json["lockedOutPoint"] = lockedOutPoint as String
        json["publicKeys"] = publicKeys.map { it.toJSON() }
        return json
    }
}