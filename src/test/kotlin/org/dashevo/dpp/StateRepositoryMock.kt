package org.dashevo.dpp

import org.bitcoinj.core.Block
import org.bitcoinj.params.TestNet3Params
import org.dashevo.dpp.contract.DataContract
import org.dashevo.dpp.document.Document
import org.dashevo.dpp.identifier.Identifier
import org.dashevo.dpp.identity.Identity

class StateRepositoryMock : StateRepository {
    override fun fetchDataContract(id: Identifier): DataContract? {
        return DataContract(id, id, 0, DataContract.SCHEMA, mutableMapOf())
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

    override fun fetchTransaction(id: String): Int {
        return 0
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

    override fun fetchLatestPlatformBlockHeader(): Block {
        return TestNet3Params.get().genesisBlock
    }

    override fun checkAssetLockTransactionOutPointExists(outPointBuffer: ByteArray) {
        TODO("Not yet implemented")
    }

    override fun storeAssetLockTransactionOutPoint(outPointBuffer: ByteArray) {
        TODO("Not yet implemented")
    }
}