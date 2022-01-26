package org.dashj.platform.dpp

import org.bitcoinj.core.Transaction
import org.bitcoinj.quorums.InstantSendLock
import org.dashj.platform.dpp.contract.DataContract
import org.dashj.platform.dpp.document.Document
import org.dashj.platform.dpp.identifier.Identifier
import org.dashj.platform.dpp.identity.Identity

open class StateRepositoryMock : StateRepository {
    override fun fetchDataContract(id: Identifier): DataContract? {
        return DataContract(0, id, id, 1, DataContract.SCHEMA, mutableMapOf())
    }

    override fun storeDataContract(dataContract: DataContract) {
        TODO("Not yet implemented")
    }

    override fun fetchDocuments(contractId: Identifier, type: String, where: Any): List<Document> {
        return listOf()
    }

    override fun storeDocument(document: Document) {
        TODO("Not yet implemented")
    }

    override fun removeDocument(contractId: Identifier, type: String, id: Identifier) {
        TODO("Not yet implemented")
    }

    override fun fetchTransaction(id: String): Transaction? {
        return null
    }

    override fun fetchIdentity(id: Identifier): Identity? {
        return null
    }

    override fun storeIdentity(identity: Identity) {
        TODO("Not yet implemented")
    }

    override fun storeIdentityPublicKeyHashes(identity: Identifier, publicKeyHashes: List<ByteArray>) {
        TODO("Not yet implemented")
    }

    override fun fetchLatestPlatformBlockHeader(): ByteArray {
        TODO("Not yet implemented")
    }

    override fun verifyInstantLock(instantLock: InstantSendLock): Boolean {
        TODO("Not yet implemented")
    }

    override fun isAssetLockTransactionOutPointAlreadyUsed(outPointBuffer: ByteArray): Boolean {
        TODO("Not yet implemented")
    }

    override fun markAssetLockTransactionOutPointAsUsed(outPointBuffer: ByteArray) {
        TODO("Not yet implemented")
    }
}
