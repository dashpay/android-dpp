/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashj.platform.dpp.identity

import org.bitcoinj.core.ECKey
import org.bitcoinj.core.Sha256Hash
import org.bitcoinj.core.Transaction
import org.bitcoinj.core.TransactionOutPoint
import org.bitcoinj.params.TestNet3Params
import org.dashj.platform.dpp.DashPlatformProtocol
import org.dashj.platform.dpp.Fixtures
import org.dashj.platform.dpp.ProtocolVersion
import org.dashj.platform.dpp.StateRepositoryMock
import org.dashj.platform.dpp.statetransition.StateTransition
import org.dashj.platform.dpp.statetransition.errors.PublicKeyMismatchError
import org.dashj.platform.dpp.toBase64
import org.dashj.platform.dpp.toHex
import org.dashj.platform.dpp.toHexString
import org.dashj.platform.dpp.util.Cbor
import org.dashj.platform.dpp.util.Converters
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class IdentityTest {
    private val stateRepository = StateRepositoryMock()
    private val dpp = DashPlatformProtocol(stateRepository)
    private val factory = IdentityFactory(dpp, stateRepository)

    @Test
    fun testIdentity() {
        val identity = Fixtures.getIdentityFixtureTwo()

        assertEquals("4mZmxva49PBb7BE7srw9o3gixvDfj1dAx1K2dmAAauGp", identity.id.toString())
        assertEquals(2, identity.publicKeys.size)
        assertEquals("AuryIuMtRrl/VviQuyLD1l4nmxi9ogPzC9LT7tdpo0di", identity.publicKeys[0].data.toBase64())
    }

    @Test
    fun testIdentityFactory() {

        val fixtureCreatedIdentity = Fixtures.getIdentityFixtureTwo()

        val publicKeys = ArrayList<IdentityPublicKey>(2)
        publicKeys.add(IdentityPublicKey(0, IdentityPublicKey.TYPES.ECDSA_SECP256K1, "AuryIuMtRrl/VviQuyLD1l4nmxi9ogPzC9LT7tdpo0di"))
        publicKeys.add(IdentityPublicKey(2, IdentityPublicKey.TYPES.ECDSA_SECP256K1, "A8AK95PYMVX5VQKzOhcVQRCUbc9pyg3RiL7jttEMDU+L"))

        val factoryCreatedIdentity = factory.create("4mZmxva49PBb7BE7srw9o3gixvDfj1dAx1K2dmAAauGp", publicKeys, 0, ProtocolVersion.latestVersion)

        assertEquals(fixtureCreatedIdentity.id, factoryCreatedIdentity.id)
        assertArrayEquals(fixtureCreatedIdentity.publicKeys[0].data, factoryCreatedIdentity.publicKeys[0].data)
        assertEquals(fixtureCreatedIdentity.getPublicKeyById(2), factoryCreatedIdentity.getPublicKeyById(2))
    }

    @Test @Disabled
    fun applyStateTransition() {
        val createTransition = Fixtures.getIdentityCreateSTFixture()

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

        assertEquals(
            "a4647479706500697369676e6174757265f66f70726f746f636f6c56657273696f6e00747369676e61747572" +
                "655075626c69634b65794964f6",
            bytes.toHexString()
        )
        assertEquals(
            "a2647479706574444154415f434f4e54524143545f4352454154456f70726f746f636f6c56657273696f6e00",
            bytes2.toHexString()
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

        assertEquals("60efcd3cdc3676ce9221f4e66435a2a4dce6d4d8e9bfe2817e073aa13bfdcf34", hash.toHex())
        assertEquals(
            "01000000a3647479706500697369676e6174757265f6747369676e61747572655075626c69634b65794964f6",
            serializedDataBytes.toHex()
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
        val incorrectOne = IdentityPublicKey(publicKeyId, IdentityPublicKey.TYPES.ECDSA_SECP256K1, incorrectPublicKey)

        assertFalse(stateTransition.verifySignature(incorrectOne))

        val incorrectTwo = IdentityPublicKey(8, IdentityPublicKey.TYPES.ECDSA_SECP256K1, incorrectPublicKey)

        assertThrows(PublicKeyMismatchError::class.java) {
            stateTransition.verifySignature(incorrectTwo)
        }
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
        val fromFixture = Fixtures.getIdentityFixtureTwo()

        val serialized = fromFixture.toBuffer()

        val fromSerialized = IdentityFactory(dpp, stateRepository).createFromBuffer(serialized)

        assertEquals(fromFixture.toJSON(), fromSerialized.toJSON())
    }

    @Test
    fun transactionIdentityIdTest() {
        val txId = "365cf2ca42e08ba45da978b872c76bdb60f98c0202f535f401fd1175beb0adfd"
        val txData = "0100000001be20a4fb4b1dc67f43f6679e1a6ad4e80e1783a99755ed7b97ae6809fd3036ef000000006b483045022100c4676acbf41257668e15b10fed03554e816a063e9e7c4a0c39b51421ae8d800c0220432b205b4717b99e14849ea441ffb216100d2c61657c9802d173c5bf6305e3340121035aa64482c28788dc1264111274ae8e0a032b037b9e614ff7aefdeb5ad559dfc6ffffffff0260410f00000000001976a914fcf52d2c425c5f736718505725890564452323c488ac40420f0000000000166a14b850573dd12769f4e286bafdd047d4ba64640f9800000000"
        val tx = Transaction(TestNet3Params.get(), Converters.fromHex(txData), 0)

        val proof = ChainAssetLockProof(0, tx.getOutput(1).outPointFor)

        val reversed = TransactionOutPoint(TestNet3Params.get(), 1, Sha256Hash.wrapReversed(tx.txId.bytes))
        val manual = Sha256Hash.twiceOf(reversed.bitcoinSerialize()).toStringBase58()
        val manual2 = proof.createIdentifier().toString()

        assertEquals("BxPnhFctj7iFguFnGnZNfdQT7YT6Acad3nGWT3ZJRsRv", manual)
        assertEquals("BxPnhFctj7iFguFnGnZNfdQT7YT6Acad3nGWT3ZJRsRv", manual2)
    }
}
