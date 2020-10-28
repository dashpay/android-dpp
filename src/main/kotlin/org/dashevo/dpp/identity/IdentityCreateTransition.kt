/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashevo.dpp.identity

import org.bitcoinj.core.Sha256Hash
import org.dashevo.dpp.identifier.Identifier
import org.dashevo.dpp.toBase64
import org.dashevo.dpp.util.HashUtils

class IdentityCreateTransition : IdentityStateTransition {

    val identityId: Identifier // base58
    val lockedOutPoint: ByteArray  // base64
    val publicKeys: MutableList<IdentityPublicKey>

    constructor(lockedOutPoint: ByteArray,
                 publicKeys: List<IdentityPublicKey>,
                 protocolVersion: Int = 0)
    : super(Types.IDENTITY_CREATE, protocolVersion) {
        this.lockedOutPoint = lockedOutPoint
        this.identityId = Identifier.from(Sha256Hash.twiceOf(lockedOutPoint))
        this.publicKeys = publicKeys.toMutableList()
    }

    constructor(rawStateTransition: MutableMap<String, Any?>)
            : super(rawStateTransition) {
        lockedOutPoint = HashUtils.byteArrayfromBase64orByteArray(rawStateTransition["lockedOutPoint"]!!)
        publicKeys = (rawStateTransition["publicKeys"] as List<Any>).map { entry -> IdentityPublicKey(entry as MutableMap<String, Any>) }.toMutableList()
        identityId = Identifier.from(Sha256Hash.twiceOf(lockedOutPoint))
    }

    override fun toObject(skipSignature: Boolean, skipIdentifiersConversion: Boolean): MutableMap<String, Any?> {
        var map = super.toObject(skipSignature, skipIdentifiersConversion)
        map["lockedOutPoint"] = lockedOutPoint
        map["publicKeys"] = publicKeys.map { it.toObject() }
        map.remove("signaturePublicKeyId")
        return map
    }

    override fun toJSON(skipSignature: Boolean): MutableMap<String, Any?> {
        var json = super.toJSON(skipSignature)
        json["lockedOutPoint"] = lockedOutPoint.toBase64()
        json["publicKeys"] = publicKeys.map { it.toJSON() }
        json.remove("signaturePublicKeyId")
        return json
    }

    fun addPublicKeys(identityPublicKeys: List<IdentityPublicKey>) = apply {
        publicKeys.addAll(identityPublicKeys)
    }

    fun getOwnerId() : Identifier {
        return identityId
    }
}