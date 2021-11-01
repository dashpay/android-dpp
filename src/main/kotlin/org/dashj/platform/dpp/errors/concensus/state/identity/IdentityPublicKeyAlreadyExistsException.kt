package org.dashj.platform.dpp.errors.concensus.state.identity

import org.dashj.platform.dpp.errors.concensus.state.StateException
import org.dashj.platform.dpp.toHex

class IdentityPublicKeyAlreadyExistsException(val publicKeyHash: ByteArray) :
    StateException("Identity public key ${publicKeyHash.toHex()} already exists") {
    constructor(arguments: List<Any>) : this(arguments[0] as ByteArray) {
        setArguments(arguments)
    }
}
