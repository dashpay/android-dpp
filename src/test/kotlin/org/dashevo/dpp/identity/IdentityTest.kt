package org.dashevo.dpp.identity

import org.bitcoinj.core.ECKey
import org.dashevo.dpp.Fixtures
import org.dashevo.dpp.statetransition.StateTransition
import org.dashevo.dpp.statetransition.errors.PublicKeyMismatchError
import org.dashevo.dpp.toBase64
import org.dashevo.dpp.toHexString
import org.dashevo.dpp.util.Cbor
import org.dashevo.dpp.util.HashUtils
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable

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
        json["type"] = StateTransition.Types.DATA_CONTRACT.value
        json["signature"] = null
        json["signaturePublicKeyId"] = null

        val json2 = HashMap<String, Any?>()

        json2["protocolVersion"] = 0
        json2["type"] = StateTransition.Types.DATA_CONTRACT

        val bytes = Cbor.encode(json)
        val bytes2 = Cbor.encode(json2)

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


        // trigger a failure to verify
        val incorrectKey = ECKey()
        val incorrectPublicKey = incorrectKey.pubKey.toBase64()
        val incorrectIdentityKey = IdentityPublicKey(publicKeyId, IdentityPublicKey.TYPES.ECDSA_SECP256K1, incorrectPublicKey, true)

        assertFalse(stateTransition.verifySignature(incorrectIdentityKey))

        incorrectIdentityKey.id = 8
        assertThrows(PublicKeyMismatchError::class.java, Executable {stateTransition.verifySignature(incorrectIdentityKey) })
    }

    @Test
    fun verifySignedIdentityTest() {
        val identitySTBytes = HashUtils.fromHex("a7647479706503697369676e6174757265785851523979496d674d4942304446517061486b696247314248476f716b5130695064637a5a48343452394f6d625a486e6b77664c424e467638696a6e62466e4238313239476a466779624a316b4c5133636a674e6b6432562b6a7075626c69634b65797381a4626964006464617461782c416e68306b5335646a706e4c4570314e446366494b464e447759484579436376695855735a62614d36797955647479706501696973456e61626c6564f56c6964656e7469747954797065016e6c6f636b65644f7574506f696e747830654c66354a4d365a6d433137795646676d4534346f72462b694c715974566739517941525a575843646a3042414141416f70726f746f636f6c56657273696f6e00747369676e61747572655075626c69634b6579496400")
        val identityST = IdentityCreateTransition(Cbor.decode(identitySTBytes))
        identityST.verifySignature(identityST.publicKeys[0])
    }
}