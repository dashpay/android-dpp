/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashevo.dpp.identity

import org.dashevo.dpp.statetransition.StateTransitionIdentitySigned

abstract class IdentityStateTransition : StateTransitionIdentitySigned {

    constructor(signaturePublicKeyId: Int?,
                              signature: String?,
                              type: Types, protocolVersion: Int)
    : super (signaturePublicKeyId, signature, type, protocolVersion)

    constructor(type: Types, protocolVersion: Int = 0) : this(null, null, type, protocolVersion)

    constructor(rawStateTransition: MutableMap<String, Any?>) : super(rawStateTransition)
}