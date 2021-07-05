package org.dashevo.dpp.statetransition

import org.bitcoinj.core.TransactionOutput
import org.dashevo.dpp.StateRepository
import org.dashevo.dpp.identity.AssetLockProof
import org.dashevo.dpp.identity.ChainAssetLockProof
import org.dashevo.dpp.identity.InstantAssetLockProof
import org.dashevo.dpp.statetransition.errors.IdentityAssetLockTransactionIsNotFoundException
import org.dashevo.dpp.statetransition.errors.UnknownAssetLockProofException
import java.lang.IllegalStateException

class AssetLockProofFactory (val stateRepository: StateRepository) {

    companion object {
        @JvmStatic
        fun createAssetLockProofInstance(rawAssetLockProof: Map<String, Any?>): AssetLockProof {
            return when (rawAssetLockProof["type"]) {
                ChainAssetLockProof.TYPE -> {
                    ChainAssetLockProof(rawAssetLockProof)
                }
                InstantAssetLockProof.TYPE -> {
                    InstantAssetLockProof(rawAssetLockProof)
                }
                else -> throw IllegalStateException("Invalid type ${rawAssetLockProof["type"]}")
            }
        }
    }

    fun fetchAssetLockTransactionOutput(assetLockProof: AssetLockProof): TransactionOutput {
        return when (assetLockProof) {
            is InstantAssetLockProof -> {
                (assetLockProof as InstantAssetLockProof).output
            }
            is ChainAssetLockProof -> {
                val outPoint = assetLockProof.outPoint

                val transaction = stateRepository.fetchTransaction(outPoint.hash.toString())
                    ?: throw IdentityAssetLockTransactionIsNotFoundException(assetLockProof.outPoint)

                transaction.outputs[outPoint.index.toInt()]
            }
            else -> throw UnknownAssetLockProofException(assetLockProof.type)
        }
    }
}