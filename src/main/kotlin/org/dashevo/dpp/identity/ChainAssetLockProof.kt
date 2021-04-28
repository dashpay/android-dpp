package org.dashevo.dpp.identity

import org.bitcoinj.core.Sha256Hash
import org.bitcoinj.core.TransactionOutPoint
import org.bitcoinj.quorums.InstantSendLock
import org.dashevo.dpp.BaseObject
import org.dashevo.dpp.identifier.Identifier
import org.dashevo.dpp.toBase64
import org.dashevo.dpp.util.HashUtils

class ChainAssetLockProof(val coreChainLockedHeight: Long,
                          val outPoint: TransactionOutPoint) : AssetLockProof() {
    companion object {
        const val TYPE = 1
    }

    override val type: Int = TYPE

    constructor(coreChainLockedHeight: Long, outPoint: ByteArray)
    : this(coreChainLockedHeight, TransactionOutPoint(null, outPoint, 0))

    constructor(rawAssetLockProof: Map<String, Any?>)
            : this(rawAssetLockProof["coreChainLockedHeight"] as Long,
                HashUtils.byteArrayfromBase64orByteArray(rawAssetLockProof["outPoint"]
                ?: error("missing outPoint field")))

    override fun getOutPoint(): ByteArray {
        val outPoint = TransactionOutPoint(outPoint.params, outPoint.index, Sha256Hash.wrap(outPoint.hash.reversedBytes))
        return outPoint.bitcoinSerialize()
    }

    override fun toObject(): Map<String, Any?> {
        return hashMapOf(
            "type" to type,
            "coreChainLockedHeight" to coreChainLockedHeight,
            "outPoint" to getOutPoint(),
        )
    }

    override fun toJSON(): Map<String, Any?> {
        return hashMapOf(
            "type" to type,
            "coreChainLockedHeight" to coreChainLockedHeight,
            "outPoint" to getOutPoint().toBase64(),
        )
    }
}