package org.dashj.platform.dpp.statetransition

import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.core.TransactionOutput
import org.dashj.platform.dpp.StateRepository
import org.dashj.platform.dpp.identity.AssetLockProof
import org.dashj.platform.dpp.identity.ChainAssetLockProof
import org.dashj.platform.dpp.identity.InstantAssetLockProof
import org.dashj.platform.dpp.statetransition.errors.IdentityAssetLockTransactionIsNotFoundException
import org.dashj.platform.dpp.statetransition.errors.UnknownAssetLockProofException
import java.lang.IllegalStateException

class AssetLockProofFactory(val stateRepository: StateRepository) {

    companion object {
        @JvmStatic
        fun createAssetLockProofInstance(
            params: NetworkParameters,
            rawAssetLockProof: Map<String, Any?>
        ): AssetLockProof {
            return when (rawAssetLockProof["type"]) {
                ChainAssetLockProof.TYPE -> {
                    ChainAssetLockProof(params, rawAssetLockProof)
                }
                InstantAssetLockProof.TYPE -> {
                    InstantAssetLockProof(params, rawAssetLockProof)
                }
                else -> throw IllegalStateException("Invalid type ${rawAssetLockProof["type"]}")
            }
        }
    }

    fun fetchAssetLockTransactionOutput(assetLockProof: AssetLockProof): TransactionOutput {
        return when (assetLockProof) {
            is InstantAssetLockProof -> {
                assetLockProof.output
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
