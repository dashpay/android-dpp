package org.dashj.platform.dpp.errors.concensus.signature

import org.dashj.platform.dpp.identity.IdentityPublicKey

class InvalidSignaturePublicKeySecurityLevelException(
    val publicKeySecurityLevel: IdentityPublicKey.SecurityLevel,
    val keySecurityLevelRequirement: IdentityPublicKey.SecurityLevel
) : SignatureException(
    "Invalid public key security level $publicKeySecurityLevel. " +
        "This state transition requires $keySecurityLevelRequirement."
) {
    constructor(arguments: List<Any>) : this(
        IdentityPublicKey.SecurityLevel.getByCode(arguments[0] as Int),
        IdentityPublicKey.SecurityLevel.getByCode(arguments[1] as Int)
    ) {
        setArguments(arguments)
    }
}
