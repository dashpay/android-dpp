/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashj.platform.dpp

import org.bitcoinj.core.Context
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.params.TestNet3Params
import org.dashj.platform.dpp.contract.ContractFactory
import org.dashj.platform.dpp.document.DocumentFactory
import org.dashj.platform.dpp.identity.IdentityFactory
import org.dashj.platform.dpp.validation.JsonSchemaValidator
import org.dashj.platform.dpp.validation.Validator

class DashPlatformProtocol(val stateRepository: StateRepository) {
    lateinit var document: DocumentFactory
    lateinit var dataContract: ContractFactory
    lateinit var identity: IdentityFactory
    var protocolVersion = ProtocolVersion.latestVersion
    lateinit var params: NetworkParameters

    constructor(stateRepository: StateRepository, params: NetworkParameters) : this(stateRepository) {
        setNetworkParameters(params)
    }

    init {
        initialize(JsonSchemaValidator())
    }

    private fun initialize(validator: Validator) {
        document = DocumentFactory(this, stateRepository)
        dataContract = ContractFactory(this, stateRepository)
        identity = IdentityFactory(this, stateRepository)
    }

    /**
     * @return the network parameters being used
     *
     * if not set previously, then the network parameters will be set using the current Context
     * if there is no Context set, then the network parameters will be set to TestNet3Params
     */

    fun getNetworkParameters(): NetworkParameters {
        if (!this::params.isInitialized) {
            try {
                val context = Context.get()
                params = context?.params ?: TestNet3Params.get()
                Context.propagate(context)
            } catch (e: IllegalStateException) {
                params = TestNet3Params.get()
            }
        }
        return params
    }

    fun setNetworkParameters(params: NetworkParameters) {
        this.params = params
    }
}
