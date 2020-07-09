/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashevo.dpp.identity

import org.dashevo.dpp.toBase58
import org.dashevo.dpp.toBase64
import org.dashevo.dpp.util.HashUtils

class IdentityCreateTransition : IdentityStateTransition {

    var identityId: String = "" // base58
    var lockedOutPoint: String?  // base64
        set(value) {
            field = value
            identityId = HashUtils.toHash(HashUtils.fromBase64(lockedOutPoint!!)).toBase58()
        }
    var publicKeys: MutableList<IdentityPublicKey>

    constructor(lockedOutPoint: String?,
                 publicKeys: List<IdentityPublicKey>,
                 protocolVersion: Int = 0)
    : super(Types.IDENTITY_CREATE, protocolVersion) {
        this.lockedOutPoint = lockedOutPoint
        this.publicKeys = publicKeys.toMutableList()
    }

    constructor(rawStateTransition: MutableMap<String, Any?>)
            : super(rawStateTransition) {
        lockedOutPoint = rawStateTransition["lockedOutPoint"] as String
        publicKeys = (rawStateTransition["publicKeys"] as List<Any>).map { entry -> IdentityPublicKey(entry as MutableMap<String, Any>) }.toMutableList()
        identityId = HashUtils.toHash(HashUtils.fromBase64(lockedOutPoint!!)).toBase64()
    }

    override fun toJSON(skipSignature: Boolean): MutableMap<String, Any?> {
        var json = super.toJSON(skipSignature)
        json["lockedOutPoint"] = lockedOutPoint as String
        json["publicKeys"] = publicKeys.map { it.toJSON() }
        json.remove("signaturePublicKeyId")
        return json
    }

    fun addPublicKeys(identityPublicKeys: List<IdentityPublicKey>) = apply {
        publicKeys.addAll(identityPublicKeys)
    }

    fun getOwnerId() : String {
        return identityId
    }
}