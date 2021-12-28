/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashj.platform.dpp.identity

import org.bitcoinj.core.NetworkParameters
import org.dashj.platform.dpp.statetransition.StateTransitionIdentitySigned

abstract class IdentityStateTransition : StateTransitionIdentitySigned {

    constructor(
        params: NetworkParameters,
        signaturePublicKeyId: Int?,
        signature: ByteArray?,
        type: Types,
        protocolVersion: Int
    ) :
        super(params, signaturePublicKeyId, signature, type, protocolVersion)

    constructor(params: NetworkParameters, type: Types, protocolVersion: Int = 0) :
        this(params, null, null, type, protocolVersion)

    constructor(params: NetworkParameters, rawStateTransition: Map<String, Any?>) :
        super(params, rawStateTransition)

    override fun isDataContractStateTransition(): Boolean {
        return false
    }

    override fun isDocumentStateTransition(): Boolean {
        return false
    }

    override fun isIdentityStateTransition(): Boolean {
        return true
    }
}
