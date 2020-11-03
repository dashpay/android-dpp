package org.dashevo.dpp

import org.dashevo.dpp.contract.ContractFactory
import org.dashevo.dpp.document.DocumentFactory
import org.dashevo.dpp.identity.IdentityFactory
import org.dashevo.dpp.validation.JsonSchemaValidator
import org.dashevo.dpp.validation.Validator

class DashPlatformProtocol(val stateRepository: StateRepository) {
    lateinit var document: DocumentFactory
    lateinit var dataContract: ContractFactory
    lateinit var identity: IdentityFactory

    init {
        initialize(JsonSchemaValidator())
    }

    private fun initialize(validator: Validator) {
        document = DocumentFactory(stateRepository)
        dataContract = ContractFactory(stateRepository)
        identity = IdentityFactory(stateRepository)
    }
}