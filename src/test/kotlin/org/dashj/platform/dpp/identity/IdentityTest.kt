/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashj.platform.dpp.identity

import org.bitcoinj.core.ECKey
import org.dashj.platform.dpp.Fixtures
import org.dashj.platform.dpp.StateRepositoryMock
import org.dashj.platform.dpp.statetransition.StateTransition
import org.dashj.platform.dpp.statetransition.errors.PublicKeyMismatchError
import org.dashj.platform.dpp.toBase64
import org.dashj.platform.dpp.toHexString
import org.dashj.platform.dpp.util.Cbor
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable

class IdentityTest {
    val stateRepository = StateRepositoryMock()

    @Test
    fun testIdentity() {
        var identity = Fixtures.getIdentityFixture()

        assertEquals("4mZmxva49PBb7BE7srw9o3gixvDfj1dAx1K2dmAAauGp", identity.id.toString())
        assertEquals(2, identity.publicKeys.size)
        assertEquals("AuryIuMtRrl/VviQuyLD1l4nmxi9ogPzC9LT7tdpo0di", identity.publicKeys[0].data.toBase64())
    }

    @Test
    fun testIdentityFactory() {

        val factory = IdentityFactory(stateRepository)

        val fixtureCreatedIdentity = Fixtures.getIdentityFixture()

        val publicKeys = ArrayList<IdentityPublicKey>(2)
        publicKeys.add(IdentityPublicKey(0, IdentityPublicKey.TYPES.ECDSA_SECP256K1, "AuryIuMtRrl/VviQuyLD1l4nmxi9ogPzC9LT7tdpo0di"))
        publicKeys.add(IdentityPublicKey(2, IdentityPublicKey.TYPES.ECDSA_SECP256K1, "A8AK95PYMVX5VQKzOhcVQRCUbc9pyg3RiL7jttEMDU+L"))

        val factoryCreatedIdentity = factory.create("4mZmxva49PBb7BE7srw9o3gixvDfj1dAx1K2dmAAauGp", publicKeys, 0, Identity.PROTOCOL_VERSION)

        assertEquals(fixtureCreatedIdentity.id, factoryCreatedIdentity.id)
        assertArrayEquals(fixtureCreatedIdentity.publicKeys[0].data, factoryCreatedIdentity.publicKeys[0].data)
        assertEquals(fixtureCreatedIdentity.getPublicKeyById(2), factoryCreatedIdentity.getPublicKeyById(2))
    }

    @Test @Disabled
    fun applyStateTransition() {
        val createTransition = Fixtures.getIdentityCreateSTFixture()

        val factory = IdentityFactory(stateRepository)

        val identity = factory.applyIdentityCreateStateTransition(createTransition)

        assertEquals("ylrObex3KikHd5h/13AW/P0yklpCyEOJt7X70cAmVOE", identity.id)
        assertTrue(createTransition.isIdentityStateTransition())
        assertFalse(createTransition.isDataContractStateTransition())
        assertFalse(createTransition.isDocumentStateTransition())
    }

    @Test
    fun nullTest() {
        val json = HashMap<String, Any?>()

        json["protocolVersion"] = 0
        json["type"] = StateTransition.Types.DATA_CONTRACT_CREATE.value
        json["signature"] = null
        json["signaturePublicKeyId"] = null

        val json2 = HashMap<String, Any?>()

        json2["protocolVersion"] = 0
        json2["type"] = StateTransition.Types.DATA_CONTRACT_CREATE

        val bytes = Cbor.encode(json)
        val bytes2 = Cbor.encode(json2)

        println(bytes.toHexString())
        println(bytes2.toHexString())
        assertEquals(
            "a4647479706500697369676e6174757265f66f70726f746f636f6c56657273696f6e00747369676e61747572655075626c69634b65794964f6",
            bytes.toHexString()
        )
    }

    /*@Test
    fun validateIdentityCreateSTDataFactory() {
        val privateKey = ECKey.fromPrivate(HashUtils.fromHex("af432c476f65211f45f48f1d42c9c0b497e56696aa1736b40544ef1a496af837"))

        val stateTransition = IdentityCreateTransition(HashUtils.fromBase64("azW1UgBiB0CmdphN6of4DbT91t0Xv3/c3YUV4CnoV/kAAAAA"),
                listOf(IdentityPublicKey(0, IdentityPublicKey.TYPES.ECDSA_SECP256K1, "w8x/v8UvcQyUFJf9AYdsGJFx6iJ0WPUBr8s4opfWW0"))
        )
        stateTransition.signByPrivateKey(privateKey)

        assertTrue(stateTransition.verifySignatureByPublicKey(privateKey))
    }*/

    @Test
    fun serializationAndSigningTest() {
        val privateKey = ECKey()
        val privateKeyBuffer = privateKey.privKeyBytes
        val privateKeyHex = privateKey.privateKeyAsHex
        val publicKey = privateKey.pubKey.toBase64()
        val publicKeyId = 1

        val stateTransition = StateTransitionMock()

        val identityPublicKey = IdentityPublicKey(publicKeyId, IdentityPublicKey.TYPES.ECDSA_SECP256K1, publicKey)

        val serializedDataBytes = stateTransition.toBuffer(false)

        val hash = stateTransition.hash()

        assertEquals("6b05d28bc9e9d7ceb53eeb42e243815359032c6b43d0657da27cfa7d1c9b63bf", hash.toHexString())
        assertEquals(
            "a4647479706500697369676e6174757265f66f70726f746f636f6c56657273696f6e00747369676e61747572655075626c69634b65794964f6",
            serializedDataBytes.toHexString()
        )

        // should return public key ID
        stateTransition.sign(identityPublicKey, privateKeyHex)

        val keyId = stateTransition.signaturePublicKeyId
        assertEquals(publicKeyId, keyId)

        val isValid = stateTransition.verifySignature(identityPublicKey)

        assertTrue(isValid)

        // trigger a failure to verify
        val incorrectKey = ECKey()
        val incorrectPublicKey = incorrectKey.pubKey.toBase64()
        val incorrectIdentityKey = IdentityPublicKey(publicKeyId, IdentityPublicKey.TYPES.ECDSA_SECP256K1, incorrectPublicKey)

        assertFalse(stateTransition.verifySignature(incorrectIdentityKey))

        val incorrectIdentityKeyTwo = IdentityPublicKey(8, IdentityPublicKey.TYPES.ECDSA_SECP256K1, incorrectPublicKey)

        assertThrows(PublicKeyMismatchError::class.java, Executable { stateTransition.verifySignature(incorrectIdentityKeyTwo) })
    }

    @Test @Disabled
    fun verifySignedIdentityTest() {
        val identityST = Fixtures.getIdentityCreateSTSignedFixture()
        assertEquals("A6AJAfRJyKuNoNvt33ygYfYh6OIYA8tF1s2BQcRA9RNg", identityST.publicKeys[0].data.toBase64())
        // TODO: fix the test. after removing isEnabled, this check failed because the test data is no longer valid
        assertTrue(identityST.verifySignatureByPublicKey(ECKey.fromPublicOnly(identityST.publicKeys[0].data)))
    }

    @Test
    fun identityRoundTrip() {
        val fromFixture = Fixtures.getIdentityFixture()

        val serialized = fromFixture.toBuffer()

        val fromSerialized = IdentityFactory(stateRepository).createFromBuffer(serialized)

        assertEquals(fromFixture.toJSON(), fromSerialized.toJSON())
    }
}
