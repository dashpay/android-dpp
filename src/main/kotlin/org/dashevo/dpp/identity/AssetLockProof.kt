package org.dashevo.dpp.identity

import org.bitcoinj.core.*
import org.dashevo.dpp.BaseObject
import org.dashevo.dpp.identifier.Identifier
import org.dashevo.dpp.util.HashUtils

abstract class AssetLockProof() : BaseObject() {

    abstract val type: Int

    abstract fun getOutPoint(): ByteArray

    fun createIdentifier(): Identifier {
        return Identifier.from(Sha256Hash.twiceOf(getOutPoint()))
    }
}