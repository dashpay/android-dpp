package org.dashj.platform.dpp.contract

import org.dashj.platform.dpp.DashPlatformProtocol
import org.dashj.platform.dpp.Fixtures.getDataContractFixture
import org.dashj.platform.dpp.ProtocolVersion
import org.dashj.platform.dpp.StateRepository
import org.dashj.platform.dpp.StateRepositoryMock
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DataContractFactorySpec {

    lateinit var dataContract: DataContract
    lateinit var rawDataContract: Map<String, Any?>
    lateinit var dpp: DashPlatformProtocol
    lateinit var factory: ContractFactory
    lateinit var stateRepository: StateRepository

    @BeforeEach
    fun beforeEach() {
        dataContract = getDataContractFixture()
        rawDataContract = dataContract.toObject()
        stateRepository = StateRepositoryMock()
        dpp = DashPlatformProtocol(stateRepository)
        factory = ContractFactory(dpp, stateRepository)
    }

    @Test
    fun `create should return new Data Contract with specified name and documents definition`() {
        // need to mock the entropy before this call
        val result = factory.create(
            dataContract.ownerId.toBuffer(),
            rawDataContract["documents"] as Map<String, Any?>
        )

        assertEquals(result.documents, dataContract.documents)
    }

    @Test
    fun `createFromObject should return new Data Contract with data from passed object`() {

        val result = factory.createFromObject(rawDataContract)

        assertEquals(result, dataContract)
    }

    @Test
    fun `createStateTransition should return new DataContractCreateTransition with passed DataContract`() {
        val result = factory.createStateTransition(dataContract)

        assertTrue(result is DataContractCreateTransition)

        assertEquals(result.protocolVersion, ProtocolVersion.latestVersion)
        assertArrayEquals(result.entropy, dataContract.entropy)
        assertEquals(result.dataContract.toObject(), dataContract.toObject())
    }
}
