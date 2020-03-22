package org.dashevo.dpp.identity

import co.nstant.`in`.cbor.CborBuilder
import co.nstant.`in`.cbor.CborEncoder
import org.bitcoinj.core.ECKey
import org.dashevo.dpp.Fixtures
import org.dashevo.dpp.statetransition.StateTransition
import org.dashevo.dpp.toBase64
import org.dashevo.dpp.toHexString
import org.dashevo.dpp.util.HashUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream

class IdentityTest {

    @Test
    fun testIdentity() {
        var identity = Fixtures.getIdentityFixture()

        assertEquals("4mZmxva49PBb7BE7srw9o3gixvDfj1dAx1K2dmAAauGp", identity.id)
        assertEquals(2, identity.publicKeys.size)
        assertEquals("AuryIuMtRrl/VviQuyLD1l4nmxi9ogPzC9LT7tdpo0di", identity.publicKeys[0].data)
        assertEquals(true, identity.publicKeys[1].isEnabled)
    }

    @Test
    fun testIdentityFactory() {

        val factory = IdentityFactory()

        val fixtureCreatedIdentity = Fixtures.getIdentityFixture()

        val publicKeys = ArrayList<IdentityPublicKey>(2)
        publicKeys.add(IdentityPublicKey(0, IdentityPublicKey.TYPES.ECDSA_SECP256K1, "AuryIuMtRrl/VviQuyLD1l4nmxi9ogPzC9LT7tdpo0di", true))
        publicKeys.add(IdentityPublicKey(2, IdentityPublicKey.TYPES.ECDSA_SECP256K1, "A8AK95PYMVX5VQKzOhcVQRCUbc9pyg3RiL7jttEMDU+L", true))

        val factoryCreatedIdentity = factory.create("4mZmxva49PBb7BE7srw9o3gixvDfj1dAx1K2dmAAauGp", Identity.IdentityType.USER, publicKeys)

        assertEquals(fixtureCreatedIdentity.id, factoryCreatedIdentity.id)
        assertEquals(fixtureCreatedIdentity.publicKeys[0].data, factoryCreatedIdentity.publicKeys[0].data)
        assertEquals(fixtureCreatedIdentity.findPublicKeyById(2), factoryCreatedIdentity.findPublicKeyById(2))

    }

    @Test
    fun applyStateTransition() {
        val createTransition = Fixtures.getIdentityCreateSTFixture()

        val factory =  IdentityFactory()

        val identity = factory.applyCreateStateTransition(createTransition)

        assertEquals("ylrObex3KikHd5h/13AW/P0yklpCyEOJt7X70cAmVOE", identity.id)
    }

    @Test
    fun nullTest() {
        val json = HashMap<String, Any?>()

        json["protocolVersion"] = 0
        json["type"] = StateTransition.Types.DATA_CONTRACT.type
        json["signature"] = null
        json["signaturePublicKeyId"] = null

        val json2 = HashMap<String, Any?>()

        json2["protocolVersion"] = 0
        json2["type"] = StateTransition.Types.DATA_CONTRACT

        val bytes = HashUtils.encode(json)
        val bytes2 = HashUtils.encode(json2)

        println(bytes.toHexString())
        println(bytes2.toHexString())
        assertEquals("a4647479706501697369676e6174757265f66f70726f746f636f6c56657273696f6e00747369676e61747572655075626c69634b65794964f6",
                bytes.toHexString())


    }

    @Test
    fun serializationAndSigningTest() {
        val privateKey = ECKey()
        val privateKeyBuffer = privateKey.privKeyBytes
        val privateKeyHex = privateKey.privateKeyAsHex
        val publicKey = privateKey.pubKey.toBase64()
        val publicKeyId = 1;

        val stateTransition = StateTransitionMock()

        val identityPublicKey = IdentityPublicKey(publicKeyId, IdentityPublicKey.TYPES.ECDSA_SECP256K1, publicKey, true)

        val serializedDataBytes = stateTransition.serialize(false)

        val hash = stateTransition.hash()

        assertEquals("60fbcdd25bfd3581f476aa45341750fbd882a247e42cac2b9dcef89d862a97c4", hash)
        assertEquals("a4647479706501697369676e6174757265f66f70726f746f636f6c56657273696f6e00747369676e61747572655075626c69634b65794964f6",
                serializedDataBytes.toHexString())

        //should return public key ID
        stateTransition.sign(identityPublicKey, privateKeyHex)

        val keyId = stateTransition.signaturePublicKeyId
        assertEquals(publicKeyId, keyId)

        val isValid = stateTransition.verifySignature(identityPublicKey);

        assertTrue(isValid)

    }
}