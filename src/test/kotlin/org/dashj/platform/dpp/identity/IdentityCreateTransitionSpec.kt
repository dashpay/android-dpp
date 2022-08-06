package org.dashj.platform.dpp.identity

import org.bitcoinj.params.TestNet3Params
import org.dashj.platform.dpp.Fixtures
import org.dashj.platform.dpp.ProtocolVersion
import org.dashj.platform.dpp.assertMapEquals
import org.dashj.platform.dpp.deepCopy
import org.dashj.platform.dpp.statetransition.StateTransition
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class IdentityCreateTransitionSpec {
    private val PARAMS = TestNet3Params.get()
    private lateinit var rawStateTransition: Map<String, Any?>
    private lateinit var stateTransition: IdentityCreateTransition

    @BeforeEach
    fun beforeEach() {
        stateTransition = Fixtures.getIdentityCreateTransitionFixture()
        rawStateTransition = stateTransition.toObject()
    }

    @Test
    fun `#constructor should create an instance with specified data`() {
        assertMapEquals(rawStateTransition["assetLockProof"] as Map<String, Any?>, stateTransition.assetLockProof.toObject())
        assertEquals(stateTransition.publicKeys, listOf(IdentityPublicKey((rawStateTransition["publicKeys"] as List<Any>)[0] as Map<String, Any?>)))
    }

    @Test
    fun `#getType should return IDENTITY_CREATE type`() {
        assertEquals(StateTransition.Types.IDENTITY_CREATE, stateTransition.type)
    }

    @Test
    fun `#setAssetLockProof should set asset lock proof`() {
        // stateTransition.assetLockProof = InstantAssetLockProof(rawStateTransition["assetLockProof"] as Map<String, Any>))
        // assertMapEquals(rawStateTransition["assetLockProof"], stateTransition.assetLockProof.toObject())
    }

    @Test
    fun `#setAssetLockProof should set identityId`() {
        // stateTransition.assetLockProof = InstantAssetLockProof(rawStateTransition["assetLockProof"] as Map<String, Any>))
        // assertEquals(stateTransition.assetLockProof.createIdentifier(), stateTransition.identityId)
    }

    @Test
    fun `#getAssetLockProof should return currently set locked OutPoint`() {
        assertMapEquals(stateTransition.assetLockProof.toObject(), rawStateTransition["assetLockProof"] as Map<String, Any?>)
    }

    @Test
    fun `#getPublicKeys should return set public keys`() {
        assertEquals((rawStateTransition["publicKeys"] as List<Any>).map { IdentityPublicKey(it as Map<String, Any?>) }, stateTransition.publicKeys)
    }

    @Test
    fun `#addPublicKeys should add more public keys`() {
        val expected = stateTransition.publicKeys.deepCopy().toMutableList()
        val publicKeys = listOf(
            IdentityPublicKey(
                0,
                IdentityPublicKey.Type.ECDSA_SECP256K1,
                ByteArray(32)
            ),
            IdentityPublicKey(
                1,
                IdentityPublicKey.Type.ECDSA_SECP256K1,
                ByteArray(32)
            )
        )

        stateTransition.addPublicKeys(publicKeys)
        expected.addAll(publicKeys)
        assertEquals(expected, stateTransition.publicKeys)
    }

    @Test
    fun `#getIdentityId should return identity id`() {
        assertEquals(stateTransition.assetLockProof.createIdentifier(), stateTransition.identityId)
    }

    @Test
    fun `#getOwnerId should return owner id`() {
        assertEquals(stateTransition.identityId, stateTransition.getOwnerId())
    }

    @Test
    fun `#toObject should return raw state transition`() {
        rawStateTransition = stateTransition.toObject()

        assertMapEquals(
            rawStateTransition,
            mapOf(
                "protocolVersion" to ProtocolVersion.latestVersion,
                "type" to StateTransition.Types.IDENTITY_CREATE.value,
                "assetLockProof" to rawStateTransition["assetLockProof"],
                "publicKeys" to rawStateTransition["publicKeys"],
                "signature" to null
            )
        )
    }

    @Test
    fun `#toObject should return raw state transition without signature`() {
        rawStateTransition = stateTransition.toObject(skipSignature = true, skipIdentifiersConversion = false)

        assertMapEquals(
            rawStateTransition,
            mapOf(
                "protocolVersion" to ProtocolVersion.latestVersion,
                "type" to StateTransition.Types.IDENTITY_CREATE.value,
                "assetLockProof" to rawStateTransition["assetLockProof"],
                "publicKeys" to rawStateTransition["publicKeys"],
            )
        )
    }

    @Test fun `#toJSON should return JSON representation of state transition`() {
        val jsonStateTransition = stateTransition.toJSON()

        assertMapEquals(
            jsonStateTransition,
            mapOf(
                "protocolVersion" to ProtocolVersion.latestVersion,
                "type" to StateTransition.Types.IDENTITY_CREATE.value,
                "assetLockProof" to stateTransition.assetLockProof.toJSON(),
                "publicKeys" to stateTransition.publicKeys.map { it.toJSON() },
                "signature" to null
            )
        )
    }

    @Test
    fun `#getModifiedDataIds should return ids of created identities`() {
        val result = stateTransition.modifiedDataIds
        assertEquals(1, result.size)
        val identityId = result[0]

        assertEquals(identityId, IdentityCreateTransition(PARAMS, rawStateTransition).identityId)
    }

    @Test
    fun `#isDataContractStateTransition should return false`() {
        assertFalse(stateTransition.isDataContractStateTransition())
    }

    @Test
    fun `#isDocumentStateTransition should return false`() {
        assertFalse(stateTransition.isDocumentStateTransition())
    }

    @Test
    fun `#isIdentityStateTransition should return true`() {
        assertTrue(stateTransition.isIdentityStateTransition())
    }
}
