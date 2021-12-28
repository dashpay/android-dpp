package org.dashj.platform.dpp.identity

import org.bitcoinj.core.Sha256Hash
import org.dashj.platform.dpp.BaseObject
import org.dashj.platform.dpp.identifier.Identifier

abstract class AssetLockProof : BaseObject() {

    abstract val type: Int

    abstract fun getOutPoint(): ByteArray

    /**
     * the identifier is the double SHA hash of the outPoint structure
     * which is the txid in big endian followed by the output index in little Endian
     */
    fun createIdentifier(): Identifier {
        return Identifier.from(Sha256Hash.twiceOf(getOutPoint()))
    }
}
