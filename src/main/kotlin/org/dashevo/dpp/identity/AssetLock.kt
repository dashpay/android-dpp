package org.dashevo.dpp.identity

import org.bitcoinj.core.*
import org.dashevo.dpp.BaseObject
import org.dashevo.dpp.identifier.Identifier
import org.dashevo.dpp.util.HashUtils

class AssetLock(val outputIndex: Long,
                val transaction: Transaction,
                val proof: InstantAssetLockProof) : BaseObject() {

    companion object {
        const val PROOF_TYPE_INSTANT: Int = 0

        enum class PROOF_CLASSES_BY_TYPES(val value: Int) {
            InstantAssetLockProof(0);

            companion object {
                private val values = values()
                fun getByCode(code: Int): PROOF_CLASSES_BY_TYPES {
                    return values.filter { it.value == code }[0]
                }
            }
        }
    }

    constructor(rawAssetLock: Map<String, Any?>)
            : this(rawAssetLock["outputIndex"] as Long,
            Transaction(null, HashUtils.byteArrayfromBase64orByteArray(rawAssetLock["transaction"]
                    ?: error("missing transaction"))),
            InstantAssetLockProof(rawAssetLock["proof"] as Map<String, Any?>))

    val output: TransactionOutput
        get() = transaction.getOutput(outputIndex)

    fun getOutPoint(): ByteArray {
        return output.outPointFor.bitcoinSerialize()
    }

    fun createIdentifier(): Identifier {
        return Identifier.from(Sha256Hash.twiceOf(getOutPoint()))
    }

    override fun toObject(): Map<String, Any?> {
        return hashMapOf(
                "transaction" to transaction.bitcoinSerialize(),
                "outputIndex" to outputIndex,
                "proof" to proof.toObject()
        )
    }

    override fun toJSON(): Map<String, Any?> {
        return hashMapOf(
                "transaction" to transaction.toStringBase64(),
                "outputIndex" to outputIndex,
                "proof" to proof.toJSON()
        )
    }
}