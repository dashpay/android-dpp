package org.dashj.platform.dpp.identity

import org.bitcoinj.core.Sha256Hash
import org.dashj.platform.dpp.BaseObject
import org.dashj.platform.dpp.identifier.Identifier

abstract class AssetLockProof() : BaseObject() {

    abstract val type: Int

    abstract fun getOutPoint(): ByteArray

    fun createIdentifier(): Identifier {
        return Identifier.from(Sha256Hash.twiceOf(getOutPoint()))
    }
}
