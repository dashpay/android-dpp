package org.dashj.platform.dpp.identity

import org.bitcoinj.core.Sha256Hash
import org.bitcoinj.core.Transaction
import org.bitcoinj.core.TransactionOutPoint
import org.bitcoinj.core.TransactionOutput
import org.bitcoinj.params.TestNet3Params
import org.bitcoinj.quorums.InstantSendLock
import org.dashj.platform.dpp.toBase64
import org.dashj.platform.dpp.util.HashUtils

class InstantAssetLockProof(
    val outputIndex: Long,
    val transaction: Transaction,
    val instantLock: InstantSendLock
) : AssetLockProof() {

    companion object {
        const val TYPE = 0
    }
    override val type: Int = TYPE

    constructor(rawAssetLockProof: Map<String, Any?>) :
    this(
        rawAssetLockProof["outputIndex"].toString().toLong(),
        Transaction(TestNet3Params.get(), HashUtils.byteArrayfromBase64orByteArray(rawAssetLockProof["transaction"] ?: error("missing transaction field"))),
        InstantSendLock(TestNet3Params.get(), HashUtils.byteArrayfromBase64orByteArray(rawAssetLockProof["instantLock"] ?: error("missing instantLock field")))
    )

    val output: TransactionOutput
        get() = transaction.getOutput(outputIndex)

    override fun getOutPoint(): ByteArray {
        val outPoint = TransactionOutPoint(output.params, output.outPointFor.index, Sha256Hash.wrap(output.outPointFor.hash.reversedBytes))
        return outPoint.bitcoinSerialize()
    }

    override fun toObject(): Map<String, Any?> {
        return hashMapOf(
            "type" to type,
            "instantLock" to instantLock.bitcoinSerialize(),
            "transaction" to transaction.bitcoinSerialize(),
            "outputIndex" to outputIndex
        )
    }

    override fun toJSON(): Map<String, Any?> {
        return hashMapOf(
            "type" to type,
            "instantLock" to instantLock.bitcoinSerialize().toBase64(),
            "transaction" to transaction.toStringBase64(),
            "outputIndex" to outputIndex
        )
    }
}
