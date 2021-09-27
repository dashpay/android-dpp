package org.dashj.platform.dpp.errors.concensus.basic.identity

import org.dashj.platform.dpp.errors.concensus.ConcensusException

class InvalidInstantAssetLockProofSignatureException() : ConcensusException("Invalid instant lock proof signature") {
    constructor(arguments: List<Any>) : this() {
        setArguments(arguments)
    }
}
