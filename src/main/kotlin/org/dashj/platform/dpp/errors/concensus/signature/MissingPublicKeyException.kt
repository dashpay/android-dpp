/**
 * Copyright (c) 2021-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashj.platform.dpp.errors.concensus.signature

class MissingPublicKeyException(val publicKeyId: ByteArray) :
    SignatureException("Public key $publicKeyId doesn't exist") {
    constructor(arguments: List<Any>) : this(arguments[0] as ByteArray) {
        setArguments(arguments)
    }
}
