package org.dashj.platform.dpp.errors.concensus.signature

import org.dashj.platform.dpp.identity.IdentityPublicKey

class PublicKeyIsDisabledException(val publicKey: IdentityPublicKey) : SignatureException("Identity key ${publicKey.id} is disabled") {
    constructor(arguments: List<Any>) : this(IdentityPublicKey(arguments[0] as Map<String, Any?>)) {
        setArguments(arguments)
    }
}
