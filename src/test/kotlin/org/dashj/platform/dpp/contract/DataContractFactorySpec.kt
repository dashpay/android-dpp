/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashj.platform.dpp.contract

import org.bitcoinj.params.TestNet3Params
import org.dashj.platform.dpp.DashPlatformProtocol
import org.dashj.platform.dpp.Fixtures.getDataContractFixture
import org.dashj.platform.dpp.ProtocolVersion
import org.dashj.platform.dpp.StateRepository
import org.dashj.platform.dpp.StateRepositoryMock
import org.dashj.platform.dpp.util.Entropy
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DataContractFactorySpec {

    private lateinit var dataContract: DataContract
    private lateinit var rawDataContract: Map<String, Any?>
    lateinit var dpp: DashPlatformProtocol
    private lateinit var factory: ContractFactory
    private lateinit var stateRepository: StateRepository

    @BeforeEach
    fun beforeEach() {
        dataContract = getDataContractFixture()
        rawDataContract = dataContract.toObject()
        stateRepository = StateRepositoryMock()
        dpp = DashPlatformProtocol(stateRepository, TestNet3Params.get())
        factory = ContractFactory(dpp, stateRepository)
    }

    @Test
    fun `create should return new Data Contract with specified name and documents definition`() {
        // need to mock the entropy before this call
        Entropy.setRandomIdentifier(dataContract.id)
        Entropy.setMockGenerate(dataContract.entropy)
        val result = factory.create(
            dataContract.ownerId.toBuffer(),
            rawDataContract["documents"] as Map<String, Any?>
        )
        result.definitions = dataContract.definitions

        assertEquals(result.toJSON(), dataContract.toJSON())
        Entropy.clearMock()
    }

    @Test
    fun `createFromObject should return new Data Contract with data from passed object`() {

        val result = factory.createFromObject(rawDataContract)

        assertEquals(result, dataContract)
    }

    @Test
    fun `createDataContractCreateTransition should return new DataContractCreateTransition with passed DataContract`() {
        val result = factory.createDataContractCreateTransition(dataContract)

        assertEquals(result.protocolVersion, ProtocolVersion.latestVersion)
        assertArrayEquals(result.entropy, dataContract.entropy)
        assertEquals(result.dataContract.toObject(), dataContract.toObject())
    }

    @Test
    fun `createDataContractUpdateTransition should return new DataContractCreateTransition with passed DataContract`() {
        val result = factory.createDataContractUpdateTransition(dataContract)

        assertEquals(result.protocolVersion, ProtocolVersion.latestVersion)
        assertEquals(result.dataContract.toObject(), dataContract.toObject())
    }
}
