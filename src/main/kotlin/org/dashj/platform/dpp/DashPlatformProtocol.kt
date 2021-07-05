package org.dashj.platform.dpp

import org.dashj.platform.dpp.contract.ContractFactory
import org.dashj.platform.dpp.document.DocumentFactory
import org.dashj.platform.dpp.identity.IdentityFactory
import org.dashj.platform.dpp.validation.JsonSchemaValidator
import org.dashj.platform.dpp.validation.Validator

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