package org.dashevo.dpp.identity

import org.bitcoinj.quorums.InstantSendLock
import org.dashevo.dpp.BaseObject
import org.dashevo.dpp.util.HashUtils

class InstantAssetLockProof(val instantLock: InstantSendLock) : BaseObject() {
    val type: Int = 0

    constructor(instantLockPayload: ByteArray) : this(InstantSendLock(null, instantLockPayload))

    constructor(rawAssetLockProof: Map<String, Any?>)
            : this(HashUtils.byteArrayfromBase64orByteArray(rawAssetLockProof["instantLock"]
            ?: error("missing instantLock field")))

    override fun toJSON(): Map<String, Any?> {
        return hashMapOf(
                "type" to type,
                "instantLock" to instantLock.toStringBase64()
        )
    }

    override fun toObject(): Map<String, Any?> {
        return hashMapOf(
                "type" to type,
                "instantLock" to instantLock.bitcoinSerialize()
        )
    }
}