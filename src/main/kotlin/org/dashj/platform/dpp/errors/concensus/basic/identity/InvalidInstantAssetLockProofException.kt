package org.dashj.platform.dpp.errors.concensus.basic.identity

import org.dashj.platform.dpp.errors.concensus.ConcensusException

class InvalidInstantAssetLockProofException(message: String) :
    ConcensusException("Invalid instant lock proof: $message") {
    constructor(arguments: List<Any>) : this(arguments[0] as String) {
        setArguments(arguments)
    }
}
