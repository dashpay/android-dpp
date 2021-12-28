package org.dashj.platform.dpp.identity

import jdk.nashorn.internal.ir.annotations.Ignore
import org.bitcoinj.core.ECKey
import org.bitcoinj.core.Transaction
import org.bitcoinj.params.TestNet3Params
import org.dashj.platform.dpp.DashPlatformProtocol
import org.dashj.platform.dpp.Fixtures.getChainAssetLockProofFixture
import org.dashj.platform.dpp.Fixtures.getIdentityFixture
import org.dashj.platform.dpp.Fixtures.getInstantAssetLockProofFixture
import org.dashj.platform.dpp.StateRepositoryMock
import org.dashj.platform.dpp.assertMapEquals
import org.dashj.platform.dpp.deepCompare
import org.dashj.platform.dpp.util.Converters
import org.easymock.EasyMock.createMock
import org.easymock.EasyMock.expect
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class IdentityFactorySpec {
    private val PARAMS = TestNet3Params.get()
    private lateinit var stateRepositoryMock: StateRepositoryMock
    lateinit var dpp: DashPlatformProtocol
    lateinit var identity: Identity
    lateinit var instantAssetLockProof: InstantAssetLockProof
    lateinit var chainAssetLockProof: ChainAssetLockProof

    @BeforeEach
    fun beforeEach() {
        val rawTransaction =
            Converters.fromHex("030000000137feb5676d0851337ea3c9a992496aab7a0b3eee60aeeb9774000b7f4bababa5000000006b483045022100d91557de37645c641b948c6cd03b4ae3791a63a650db3e2fee1dcf5185d1b10402200e8bd410bf516ca61715867666d31e44495428ce5c1090bf2294a829ebcfa4ef0121025c3cc7fbfc52f710c941497fd01876c189171ea227458f501afcb38a297d65b4ffffffff021027000000000000166a14152073ca2300a86b510fa2f123d3ea7da3af68dcf77cb0090a0000001976a914152073ca2300a86b510fa2f123d3ea7da3af68dc88ac00000000")

        stateRepositoryMock = createMock(StateRepositoryMock::class.java)

        expect(stateRepositoryMock.fetchTransaction("")).andReturn(
            Transaction(PARAMS, rawTransaction, 0)
        )

        dpp = DashPlatformProtocol(stateRepositoryMock, PARAMS)

        chainAssetLockProof = getChainAssetLockProofFixture()
        instantAssetLockProof = getInstantAssetLockProofFixture()
        identity = getIdentityFixture()
        identity.id = instantAssetLockProof.createIdentifier()
        identity.assetLockProof = instantAssetLockProof
        identity.balance = 0
    }

    @Test
    fun `#create should create Identity`() {
        val publicKeys = identity.publicKeys.map {
            val map = it.toObject().toMutableMap()
            map["key"] = ECKey.fromPublicOnly(it.data)
            map
        }

        val result = dpp.identity.create(
            instantAssetLockProof,
            publicKeys,
        )

        assertEquals(identity.toObject(), result.toObject())
    }

    @Test
    fun `#createFromObject should create Identity from plain object`() {
        val result = dpp.identity.createFromObject(identity.toObject())
        assertEquals(identity.toObject(), result.toObject())
    }

    @Test @Ignore // This fails due to different list order of public keys?
    fun `#createFromBuffer should create Identity from string`() {
        val result = dpp.identity.createFromBuffer(identity.toBuffer())
        assertTrue(identity.toObject().deepCompare(result.toObject()))
        assertMapEquals(identity.toObject(), result.toObject())
    }

    @Test
    fun `#validate should validate Identity`() {
        val result = dpp.identity.validate(identity)
        assertTrue(result.isValid())
    }

    @Test
    fun `#createInstantAssetLockProof should create instant asset lock proof`() {
        val instantLock = instantAssetLockProof.instantLock
        val assetLockTransaction = instantAssetLockProof.transaction
        val outputIndex = instantAssetLockProof.outputIndex

        val result = dpp.identity.createInstantAssetLockProof(
            instantLock,
            assetLockTransaction,
            outputIndex,
        )

        assertEquals(instantLock, result.instantLock)
        assertEquals(assetLockTransaction, result.transaction)
        assertEquals(outputIndex, result.outputIndex)
    }

    @Test
    fun `#createChainAssetLockProof should create chain asset lock proof`() {
        val coreChainLockedHeight = chainAssetLockProof.coreChainLockedHeight
        val outPoint = chainAssetLockProof.getOutPoint()

        val result = dpp.identity.createChainAssetLockProof(
            coreChainLockedHeight,
            outPoint
        )

        assertEquals(coreChainLockedHeight, result.coreChainLockedHeight)
        assertArrayEquals(outPoint, result.getOutPoint())
    }

    @Test
    fun `#createIdentityCreateTransition should create IdentityCreateTransition from Identity model`() {
        val stateTransition = dpp.identity.createIdentityCreateTransition(identity)

        assertEquals(identity.publicKeys, stateTransition.publicKeys)
        assertArrayEquals(
            instantAssetLockProof.toObject()["instantLock"] as ByteArray,
            stateTransition.assetLockProof.toObject()["instantLock"] as ByteArray
        )
        assertMapEquals(instantAssetLockProof.toObject(), stateTransition.assetLockProof.toObject())
    }

    @Test
    fun `#createIdentityTopUpTransition should create IdentityTopUpTransition from identity id and outpoint`() {
        val stateTransition = dpp.identity.createIdentityTopUpTransition(
            identity.id,
            instantAssetLockProof,
        )
        assertMapEquals(instantAssetLockProof.toObject(), stateTransition.assetLock.toObject())
        assertEquals(identity.id, stateTransition.identityId)
    }
}
