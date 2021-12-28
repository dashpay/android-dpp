package org.dashj.platform.dpp.identity

import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.core.Sha256Hash
import org.bitcoinj.core.TransactionOutPoint
import org.dashj.platform.dpp.toBase64
import org.dashj.platform.dpp.util.Converters

class ChainAssetLockProof(
    val coreChainLockedHeight: Long,
    val outPoint: TransactionOutPoint
) : AssetLockProof() {
    companion object {
        const val TYPE = 1
        const val OUTPOINT_HASH_SIZE = 32
        const val OUTPOINT_SIZE = 36
    }

    override val type: Int = TYPE

    constructor(params: NetworkParameters, coreChainLockedHeight: Long, outPoint: ByteArray) :
        this(
            coreChainLockedHeight,
            TransactionOutPoint(
                params,
                outPoint.sliceArray(0 until OUTPOINT_HASH_SIZE).reversedArray().plus(
                    outPoint.sliceArray(OUTPOINT_HASH_SIZE until OUTPOINT_SIZE)
                ),
                0
            )
        )

    constructor(params: NetworkParameters, rawAssetLockProof: Map<String, Any?>) :
        this(
            params,
            rawAssetLockProof["coreChainLockedHeight"].toString().toLong(),
            Converters.byteArrayFromBase64orByteArray(
                rawAssetLockProof["outPoint"]
                    ?: error("missing outPoint field")
            )
        )

    // the outPoint format required must have the transaction hash in Big Endian Format
    override fun getOutPoint(): ByteArray {
        val outPoint = TransactionOutPoint(
            outPoint.params,
            outPoint.index,
            Sha256Hash.wrap(outPoint.hash.reversedBytes)
        )
        return outPoint.bitcoinSerialize()
    }

    override fun toObject(): Map<String, Any?> {
        return hashMapOf(
            "type" to type,
            "coreChainLockedHeight" to coreChainLockedHeight,
            "outPoint" to getOutPoint()
        )
    }

    override fun toJSON(): Map<String, Any?> {
        return hashMapOf(
            "type" to type,
            "coreChainLockedHeight" to coreChainLockedHeight,
            "outPoint" to getOutPoint().toBase64()
        )
    }
}
