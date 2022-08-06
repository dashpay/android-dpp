package org.dashj.platform.dpp.errors.concensus.state.identity

import org.dashj.platform.dpp.errors.concensus.state.StateException

class DuplicatedIdentityPublicKeyException(val duplicatedPublicKeyIds: Set<Int>) :
    StateException("Duplicated public keys ${duplicatedPublicKeyIds.joinToString(", ")} found") {
    constructor(arguments: List<Any>) : this((arguments[0] as List<Int>).toSet()) {
        setArguments(arguments)
    }
}
