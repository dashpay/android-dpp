/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashj.platform.dpp.identity

import org.bitcoinj.core.NetworkParameters
import org.dashj.platform.dpp.identifier.Identifier

class IdentityUpdateTransition : IdentityStateTransition {

    val identityId: Identifier
    val revision: Int
    val addPublicKeys: List<IdentityPublicKey>?
    val disablePublicKeys: List<Int>?
    val publicKeysDisabledAt: Long?

    /** returns id of created identity */
    override val modifiedDataIds: List<Identifier>
        get() = listOf(identityId)

    constructor(
        params: NetworkParameters,
        identityId: Identifier,
        revision: Int,
        addPublicKeys: List<IdentityPublicKey>?,
        disablePublicKeys: List<Int>?,
        publicKeysDisabledAt: Long?,
        protocolVersion: Int = 0
    ) :
        super(params, Types.IDENTITY_UPDATE, protocolVersion) {
            this.identityId = identityId
            this.revision = revision
            this.addPublicKeys = addPublicKeys
            this.disablePublicKeys = disablePublicKeys
            this.publicKeysDisabledAt = publicKeysDisabledAt
        }

    constructor(params: NetworkParameters, rawStateTransition: MutableMap<String, Any?>) :
        super(params, rawStateTransition) {
            identityId = Identifier.from(rawStateTransition["identityId"]!!)
            revision = rawStateTransition["revision"] as Int
            addPublicKeys = (rawStateTransition["addPublicKeys"] as List<Any>).map { IdentityPublicKey(it as Map<String, Any>) }
            disablePublicKeys = rawStateTransition["disablePublicKeys"] as List<Int>
            publicKeysDisabledAt = rawStateTransition["publicKeysDisabledAt"] as Long
        }

    override fun toObject(skipSignature: Boolean, skipIdentifiersConversion: Boolean): MutableMap<String, Any?> {
        val map = super.toObject(skipSignature, skipIdentifiersConversion)
        map["identityId"] = identityId
        map["revision"] = revision

        publicKeysDisabledAt?.run {
            map["publicKeysDisabledAt"] = this
        }

        addPublicKeys?.run {
            map["addPublicKeys"] = map { toObject() }
        }

        disablePublicKeys?.run {
            map["disablePublicKeys"] = this
        }

        if (!skipIdentifiersConversion) {
            map["identityId"] = identityId.toBuffer()
        }

        return map
    }

    override fun toJSON(skipSignature: Boolean): MutableMap<String, Any?> {
        val json = super.toJSON(skipSignature)
        json["identityId"] = identityId.toString()
        json["revision"] = revision
        publicKeysDisabledAt?.run {
            json["publicKeysDisabledAt"] = this
        }

        addPublicKeys?.run {
            json["addPublicKeys"] = map { toJSON() }
        }

        disablePublicKeys?.run {
            json["disablePublicKeys"] = this
        }
        return json
    }

    override fun getKeySecurityLevelRequirement(): IdentityPublicKey.SecurityLevel {
        return IdentityPublicKey.SecurityLevel.MASTER
    }
}
