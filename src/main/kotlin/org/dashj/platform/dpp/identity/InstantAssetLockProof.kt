package org.dashj.platform.dpp.identity

import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.core.Sha256Hash
import org.bitcoinj.core.Transaction
import org.bitcoinj.core.TransactionOutPoint
import org.bitcoinj.core.TransactionOutput
import org.bitcoinj.quorums.InstantSendLock
import org.dashj.platform.dpp.toBase64
import org.dashj.platform.dpp.util.Converters

class InstantAssetLockProof(
    val outputIndex: Long,
    val transaction: Transaction,
    val instantLock: InstantSendLock
) : AssetLockProof() {

    companion object {
        const val TYPE = 0
    }
    override val type: Int = TYPE

    constructor(params: NetworkParameters, rawAssetLockProof: Map<String, Any?>) :
        this(
            rawAssetLockProof["outputIndex"].toString().toLong(),
            Transaction(
                params,
                Converters.byteArrayFromBase64orByteArray(
                    rawAssetLockProof["transaction"] ?: error("missing transaction field")
                )
            ),
            InstantSendLock(
                params,
                Converters.byteArrayFromBase64orByteArray(
                    rawAssetLockProof["instantLock"] ?: error("missing instantLock field")
                ),
                InstantSendLock.ISDLOCK_VERSION // Core 0.17
            )
        )

    val output: TransactionOutput
        get() = transaction.getOutput(outputIndex)

    // the outPoint format required must have the transaction hash in Big Endian Format
    override fun getOutPoint(): ByteArray {
        val outPoint = TransactionOutPoint(
            output.params, output.outPointFor.index,
            Sha256Hash.wrap(output.outPointFor.hash.reversedBytes)
        )
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
