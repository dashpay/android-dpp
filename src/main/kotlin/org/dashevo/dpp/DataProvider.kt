package org.dashevo.dpp

import org.dashevo.dpp.contract.DataContract
import org.dashevo.dpp.document.Document
import org.dashevo.dpp.identity.Identity


/**
 *
 */
interface DataProvider {
    /**
     * Fetch Data Contract by ID
     *
     */
    fun fetchDataContract(contractId: String): DataContract?

    /**
     * Fetch Documents by Data Contract ID and type
     *
     */
    fun fetchDocuments(contractId: String, type: String, where: Any): List<Document>

    /**
     * Fetch transaction by ID
     *
     */
    fun fetchTransaction(id: String): Int

    /**
     * Fetch identity by ID
     *
     */
    fun fetchIdentity(id: String): Identity?
}

