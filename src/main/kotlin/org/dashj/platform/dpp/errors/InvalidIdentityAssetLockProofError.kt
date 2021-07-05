package org.dashevo.dpp.errors

class InvalidIdentityAssetLockProofError(message: String)
    : Exception("Invalid asset lock proof: $message")