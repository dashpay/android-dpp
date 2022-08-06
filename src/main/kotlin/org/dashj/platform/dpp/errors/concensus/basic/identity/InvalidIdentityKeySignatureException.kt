/**
 * Copyright (c) 2022-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashj.platform.dpp.errors.concensus.basic.identity

import org.dashj.platform.dpp.errors.concensus.ConcensusException

class InvalidIdentityKeySignatureException(publicKeyId: Int) :
    ConcensusException("Identity key $publicKeyId has invalid signature") {

    constructor(arguments: List<Any>) : this(arguments[0] as Int) {
        setArguments(arguments)
    }
}
