/**
 * Copyright (c) 2021-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashj.platform.dpp.errors.concensus.signature

import org.dashj.platform.dpp.identity.IdentityPublicKey

class InvalidIdentityPublicKeyTypeException(val type: Int) :
    SignatureException("Invalid identity public key type $type") {
    constructor(arguments: List<Any>) : this(arguments[0] as Int) {
        setArguments(arguments)
    }
    constructor(type: IdentityPublicKey.Type) : this(type.value)
}
