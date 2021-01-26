/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashevo.dpp.identity

import org.dashevo.dpp.identifier.Identifier

class IdentityTopupTransition : IdentityStateTransition {

    val identityId: Identifier
    var assetLock: AssetLock

    constructor(identityId: Identifier,
                assetLock: AssetLock,
                protocolVersion: Int = 0)
            : super(Types.IDENTITY_TOP_UP, protocolVersion) {
        this.assetLock = assetLock
        this.identityId = identityId
    }

    constructor(rawStateTransition: MutableMap<String, Any?>)
            : super(rawStateTransition) {
        assetLock = AssetLock(rawStateTransition["assetLock"] as Map<String, Any?>)
        identityId = Identifier.from(rawStateTransition["identityId"]!!)
    }

    override fun toObject(skipSignature: Boolean, skipIdentifiersConversion: Boolean): MutableMap<String, Any?> {
        val map = super.toObject(skipSignature, skipIdentifiersConversion)
        map["identityId"] = identityId
        map["assetLock"] = assetLock.toObject()

        if (!skipIdentifiersConversion) {
            map["identityId"] = identityId.toBuffer()
        }

        return map
    }

    override fun toJSON(skipSignature: Boolean): MutableMap<String, Any?> {
        val json = super.toJSON(skipSignature)
        json["assetLock"] = assetLock.toJSON()
        json["identityId"] = identityId.toString()
        return json
    }

    fun getOwnerId(): Identifier {
        return identityId
    }
}