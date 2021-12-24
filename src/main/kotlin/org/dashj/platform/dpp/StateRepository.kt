/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashj.platform.dpp

import org.bitcoinj.core.Transaction
import org.bitcoinj.quorums.InstantSendLock
import org.dashj.platform.dpp.contract.DataContract
import org.dashj.platform.dpp.document.Document
import org.dashj.platform.dpp.identifier.Identifier
import org.dashj.platform.dpp.identity.Identity

/**
 *
 */
interface StateRepository {
    /**
     * Fetch Data Contract by ID
     *
     */
    fun fetchDataContract(id: Identifier): DataContract?

    fun storeDataContract(dataContract: DataContract)

    /**
     * Fetch Documents by Data Contract ID and type
     *
     */
    fun fetchDocuments(contractId: Identifier, type: String, where: Any): List<Document>

    fun storeDocument(document: Document)

    fun removeDocument(contractId: Identifier, type: String, id: Identifier)
    /**
     * Fetch transaction by ID
     *
     */
    fun fetchTransaction(id: String): Transaction?

    /**
     * Fetch identity by ID
     *
     */
    fun fetchIdentity(id: Identifier): Identity?

    fun storeIdentity(identity: Identity)

    fun storeIdentityPublicKeyHashes(identity: Identifier, publicKeyHashes: List<ByteArray>)

    fun fetchLatestPlatformBlockHeader(): ByteArray

    fun verifyInstantLock(instantLock: InstantSendLock): Boolean

    fun isAssetLockTransactionOutPointAlreadyUsed(outPointBuffer: ByteArray): Boolean

    fun markAssetLockTransactionOutPointAsUsed(outPointBuffer: ByteArray)
}
