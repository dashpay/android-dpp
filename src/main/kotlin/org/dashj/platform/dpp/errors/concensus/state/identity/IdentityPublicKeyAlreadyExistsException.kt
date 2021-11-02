/**
 * Copyright (c) 2021-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashj.platform.dpp.errors.concensus.state.identity

import org.dashj.platform.dpp.errors.concensus.state.StateException
import org.dashj.platform.dpp.toHex

class IdentityPublicKeyAlreadyExistsException(val publicKeyHash: ByteArray) :
    StateException("Identity public key ${publicKeyHash.toHex()} already exists") {
    constructor(arguments: List<Any>) : this(arguments[0] as ByteArray) {
        setArguments(arguments)
    }
}
