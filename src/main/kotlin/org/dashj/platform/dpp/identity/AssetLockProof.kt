package org.dashj.platform.dpp.identity

import org.bitcoinj.core.*
import org.dashj.platform.dpp.BaseObject
import org.dashj.platform.dpp.identifier.Identifier
import org.dashj.platform.dpp.util.HashUtils

abstract class AssetLockProof() : BaseObject() {

    abstract val type: Int

    abstract fun getOutPoint(): ByteArray

    fun createIdentifier(): Identifier {
        return Identifier.from(Sha256Hash.twiceOf(getOutPoint()))
    }
}