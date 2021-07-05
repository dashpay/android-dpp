package org.dashj.platform.dpp.errors

class InvalidIdentityAssetLockProofError(message: String)
    : Exception("Invalid asset lock proof: $message")