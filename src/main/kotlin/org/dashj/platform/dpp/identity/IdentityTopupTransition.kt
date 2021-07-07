/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashj.platform.dpp.identity

import org.dashj.platform.dpp.identifier.Identifier
import org.dashj.platform.dpp.statetransition.AssetLockProofFactory

class IdentityTopupTransition : IdentityStateTransition {

    val identityId: Identifier
    var assetLock: AssetLockProof
    /** returns id of created identity */
    override val modifiedDataIds: List<Identifier>
        get() = listOf(identityId)

    constructor(
        identityId: Identifier,
        assetLock: AssetLockProof,
        protocolVersion: Int = 0
    ) :
        super(Types.IDENTITY_TOP_UP, protocolVersion) {
            this.assetLock = assetLock
            this.identityId = identityId
        }

    constructor(rawStateTransition: MutableMap<String, Any?>) :
        super(rawStateTransition) {
            assetLock = AssetLockProofFactory.createAssetLockProofInstance(rawStateTransition["assetLock"] as Map<String, Any?>)
            identityId = Identifier.from(rawStateTransition["identityId"]!!)
        }

    override fun toObject(skipSignature: Boolean, skipIdentifiersConversion: Boolean): MutableMap<String, Any?> {
        val map = super.toObject(skipSignature, skipIdentifiersConversion)
        map["identityId"] = identityId
        map["assetLockProof"] = assetLock.toObject()

        if (!skipIdentifiersConversion) {
            map["identityId"] = identityId.toBuffer()
        }

        return map
    }

    override fun toJSON(skipSignature: Boolean): MutableMap<String, Any?> {
        val json = super.toJSON(skipSignature)
        json["assetLockProof"] = assetLock.toJSON()
        json["identityId"] = identityId.toString()
        return json
    }

    fun getOwnerId(): Identifier {
        return identityId
    }
}
