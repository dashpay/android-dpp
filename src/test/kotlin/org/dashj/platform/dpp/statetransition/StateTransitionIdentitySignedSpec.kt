/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashj.platform.dpp.statetransition

import org.bitcoinj.core.ECKey
import org.bitcoinj.core.Utils
import org.bitcoinj.params.TestNet3Params
import org.dashj.platform.dpp.assertMapEquals
import org.dashj.platform.dpp.identity.IdentityPublicKey
import org.dashj.platform.dpp.identity.StateTransitionMock
import org.dashj.platform.dpp.statetransition.errors.InvalidSignaturePublicKeyException
import org.dashj.platform.dpp.statetransition.errors.InvalidSignatureTypeException
import org.dashj.platform.dpp.statetransition.errors.PublicKeyMismatchError
import org.dashj.platform.dpp.statetransition.errors.PublicKeySecurityLevelNotMetException
import org.dashj.platform.dpp.statetransition.errors.StateTransitionIsNotSignedError
import org.dashj.platform.dpp.statetransition.errors.WrongPublicKeyPurposeException
import org.dashj.platform.dpp.toBase64
import org.dashj.platform.dpp.toHex
import org.dashj.platform.dpp.util.Converters
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class StateTransitionIdentitySignedSpec {
    private val PARAMS = TestNet3Params.get()
    private lateinit var stateTransition: StateTransitionIdentitySigned
    var protocolVersion: Int = 1
    lateinit var privateKey: ECKey
    lateinit var privateKeyWIF: String
    lateinit var privateKeyHex: String
    private var publicKeyId: Int = 1
    private lateinit var identityPublicKey: IdentityPublicKey

    @BeforeEach
    fun beforeEach() {
        privateKey = ECKey()
        privateKeyWIF = privateKey.getPrivateKeyAsWiF(PARAMS)
        privateKeyHex = privateKey.privateKeyAsHex
        protocolVersion = 1
        publicKeyId = 1
        stateTransition = StateTransitionMock(protocolVersion) // EasyMock.createMock(StateTransitionIdentitySigned::class.java)

        identityPublicKey = IdentityPublicKey(
            publicKeyId,
            IdentityPublicKey.TYPES.ECDSA_SECP256K1,
            IdentityPublicKey.Purpose.AUTHENTICATION,
            IdentityPublicKey.SecurityLevel.MASTER,
            privateKey.pubKey,
            false
        )
    }

    @Test
    fun `verifySignatureByPublicKey should throw StateTransitionIsNotSignedError error if transition is not signed`() {
        val publicKey = ECKey.fromPublicOnly(Converters.fromBase64("A1eUrJ7lM6F1m6dbIyk+vXimKfzki+QRMHMwoAmggt6L"))
        try {
            stateTransition.verifySignatureByPublicKey(publicKey)

            fail<Nothing>("should throw StateTransitionIsNotSignedError")
        } catch (e: Exception) {
            assertTrue(e is StateTransitionIsNotSignedError)
            assertEquals((e as StateTransitionIsNotSignedError).stateTransition, stateTransition)
        }
    }

    @Test
    fun `#toObject should return raw state transition`() {
        val rawStateTransition = stateTransition.toObject()

        assertMapEquals(
            rawStateTransition,
            mapOf(
                "protocolVersion" to protocolVersion,
                "signature" to null,
                "signaturePublicKeyId" to null,
                "type" to 0
            )
        )
    }

    @Test
    fun `#toObject should return raw state transition without signature `() {
        val rawStateTransition = stateTransition.toObject(skipSignature = true, false)

        assertMapEquals(
            rawStateTransition,
            mapOf(
                "protocolVersion" to protocolVersion,
                "type" to 0,
            )
        )
    }

    @Test
    fun `#toJSON should return state transition as JSON`() {
        val jsonStateTransition = stateTransition.toJSON()

        assertMapEquals(
            jsonStateTransition,
            mapOf(
                "signaturePublicKeyId" to null,
                "signature" to null,
                "protocolVersion" to protocolVersion,
                "type" to 0
            )
        )
    }

    @Test
    fun `#hash should return serialized hash`() {
        val hash = stateTransition.hash()

        assertEquals("60efcd3cdc3676ce9221f4e66435a2a4dce6d4d8e9bfe2817e073aa13bfdcf34", hash.toHex())
    }

    @Test
    fun `#toBuffer should return serialized data`() {
        val serializedData = stateTransition.toBuffer()
        assertEquals(
            "01000000a3647479706500697369676e6174757265f6747369676e61747572655075626c69634b65794964f6",
            serializedData.toHex()
        )
    }

    @Test
    fun `#toBuffer should return serialized data without signature data`() {
        val serializedData = stateTransition.toBuffer(true)
        assertEquals("01000000a1647479706500", serializedData.toHex())
    }

    @Test
    fun `#getSignaturePublicKeyId should return public key ID`() {
        stateTransition.sign(identityPublicKey, privateKeyHex)

        val keyId = stateTransition.signaturePublicKeyId
        assertEquals(publicKeyId, keyId)
    }

    @Test
    fun `#sign should sign data and validate signature with private key in hex format`() {
        stateTransition.sign(identityPublicKey, privateKeyHex)

        val isValid = stateTransition.verifySignature(identityPublicKey)

        assertTrue(isValid)
    }

    @Test
    fun `#sign should sign data and validate signature with private key in buffer format`() {
        stateTransition.sign(identityPublicKey, privateKeyWIF)

        val isValid = stateTransition.verifySignature(identityPublicKey)

        assertTrue(isValid)
    }

    @Test
    fun `#sign should sign data and validate signature with ECDSA_HASH160 identityPublicKey`() {
        val newIdentityPublicKey = IdentityPublicKey(
            identityPublicKey.id,
            IdentityPublicKey.TYPES.ECDSA_HASH160,
            identityPublicKey.purpose,
            identityPublicKey.securityLevel,
            Utils.sha256hash160(identityPublicKey.data),
            identityPublicKey.readOnly
        )

        stateTransition.sign(newIdentityPublicKey, privateKeyHex)

        val isValid = stateTransition.verifySignature(newIdentityPublicKey)

        assertTrue(isValid)
    }

    @Test
    fun `#sign should throw an error if we try to sign with wrong public key`() {
        val publicKey = ECKey().pubKey

        val newIdentityPublicKey = IdentityPublicKey(
            identityPublicKey.id,
            IdentityPublicKey.TYPES.ECDSA_HASH160,
            identityPublicKey.purpose,
            identityPublicKey.securityLevel,
            publicKey,
            identityPublicKey.readOnly
        )

        try {
            stateTransition.sign(newIdentityPublicKey, privateKeyHex)

            fail("Should throw InvalidSignaturePublicKeyException")
        } catch (e: InvalidSignaturePublicKeyException) {
            assertEquals(newIdentityPublicKey.data.toBase64(), e.signaturePublicKey)
        }
    }

    @Test
    fun `#sign should throw InvalidSignatureTypeError if signature type is not equal ECDSA`() {

        val newIdentityPublicKey = IdentityPublicKey(
            identityPublicKey.id,
            IdentityPublicKey.TYPES.BLS12_381,
            identityPublicKey.purpose,
            identityPublicKey.securityLevel,
            identityPublicKey.data,
            identityPublicKey.readOnly
        )

        try {
            stateTransition.sign(newIdentityPublicKey, privateKeyHex)
            fail("Should throw InvalidSignatureTypeError")
        } catch (e: InvalidSignatureTypeException) {
            assertEquals(newIdentityPublicKey.type, e.signatureType)
        }
    }

    @Test
    fun `#sign should throw an error if the key security level is not met`() {
        // EasyMock.expect(stateTransition.getRequiredKeySecurityLevel()).andReturn(
        //    IdentityPublicKey.SecurityLevel.MASTER
        // )

        val newIdentityPublicKey = IdentityPublicKey(
            identityPublicKey.id,
            identityPublicKey.type,
            identityPublicKey.purpose,
            IdentityPublicKey.SecurityLevel.MEDIUM,
            identityPublicKey.data,
            identityPublicKey.readOnly
        )

        try {
            stateTransition.sign(newIdentityPublicKey, privateKeyHex)

            fail("Should throw PublicKeySecurityLevelNotMetError")
        } catch (e: PublicKeySecurityLevelNotMetException) {
            assertEquals(newIdentityPublicKey.securityLevel, e.publicKeySecurityLevel)
            assertEquals(IdentityPublicKey.SecurityLevel.MASTER, e.requiredSecurityLevel)
        }
    }

    @Test
    fun `#sign should throw an error if the key purpose is not authentication`() {
        val newIdentityPublicKey = IdentityPublicKey(
            identityPublicKey.id,
            identityPublicKey.type,
            IdentityPublicKey.Purpose.ENCRYPTION,
            IdentityPublicKey.SecurityLevel.MASTER,
            identityPublicKey.data,
            identityPublicKey.readOnly
        )

        try {
            stateTransition.sign(newIdentityPublicKey, privateKeyHex)

            fail("Should throw WrongPublicKeyPurposeError")
        } catch (e: WrongPublicKeyPurposeException) {
            assertEquals(newIdentityPublicKey.purpose, e.publicKeyPurpose)
            assertEquals(IdentityPublicKey.Purpose.AUTHENTICATION, e.keyPurposeRequirement)
        }
    }

    @Test
    fun `#signByPrivateKey should sign and validate with private key`() {
        privateKeyHex = "9b67f852093bc61cea0eeca38599dbfba0de28574d2ed9b99d10d33dc1bde7b2"
        stateTransition.signByPrivateKey(privateKeyHex)
        assertTrue(stateTransition.signature != null)
    }

    @Test
    fun `#verifySignature should validate signature`() {
        stateTransition.sign(identityPublicKey, privateKeyHex)
        val isValid = stateTransition.verifySignature(identityPublicKey)

        assertTrue(isValid)
    }

    @Test
    fun `#verifySignature should throw an StateTransitionIsNotSignedError error if transition is not signed`() {
        try {
            stateTransition.verifySignature(identityPublicKey)

            fail("should throw StateTransitionIsNotSignedError")
        } catch (e: StateTransitionIsNotSignedError) {
            assertEquals(stateTransition, e.stateTransition)
        }
    }

    @Test
    fun `#verifySignature should throw an PublicKeyMismatchError error if public key id not equals public key id in state transition`() {
        stateTransition.sign(identityPublicKey, privateKeyHex)

        val newIdentityPublicKey = IdentityPublicKey(
            identityPublicKey.id + 1,
            identityPublicKey.type,
            identityPublicKey.purpose,
            identityPublicKey.securityLevel,
            identityPublicKey.data,
            identityPublicKey.readOnly
        )

        try {
            stateTransition.verifySignature(newIdentityPublicKey)

            fail("should throw PublicKeyMismatchError")
        } catch (e: PublicKeyMismatchError) {
            assertEquals(newIdentityPublicKey, e.identityPublicKey)
        }
    }

    @Test
    fun `#verifySignature should not verify signature with wrong public key`() {
        stateTransition.sign(identityPublicKey, privateKeyHex)
        val publicKey = ECKey().pubKey

        val newIdentityPublicKey = IdentityPublicKey(
            identityPublicKey.id,
            identityPublicKey.type,
            identityPublicKey.purpose,
            identityPublicKey.securityLevel,
            publicKey,
            identityPublicKey.readOnly
        )

        val isValid = stateTransition.verifySignature(newIdentityPublicKey)

        assertFalse(isValid)
    }

    @Test
    fun `#verifySignature should throw an error if the key security level is not met`() {
        stateTransition.sign(identityPublicKey, privateKeyHex)

        // Set key security level after the signing, since otherwise .sign method won't work
        val newIdentityPublicKey = IdentityPublicKey(
            identityPublicKey.id,
            identityPublicKey.type,
            identityPublicKey.purpose,
            IdentityPublicKey.SecurityLevel.MEDIUM,
            identityPublicKey.data,
            identityPublicKey.readOnly
        )

        try {
            stateTransition.verifySignature(newIdentityPublicKey)

            fail("Should throw PublicKeySecurityLevelNotMetError")
        } catch (e: PublicKeySecurityLevelNotMetException) {
            assertEquals(newIdentityPublicKey.securityLevel, e.publicKeySecurityLevel)
            assertEquals(IdentityPublicKey.SecurityLevel.MASTER, e.requiredSecurityLevel)
        }
    }

    @Test
    fun `#verifySignature should throw an error if the key purpose is not equal to authentication`() {
        stateTransition.sign(identityPublicKey, privateKeyHex)

        // Set key security level after the signing, since otherwise .sign method won't work
        val newIdentityPublicKey = IdentityPublicKey(
            identityPublicKey.id,
            identityPublicKey.type,
            IdentityPublicKey.Purpose.ENCRYPTION,
            identityPublicKey.securityLevel,
            identityPublicKey.data,
            identityPublicKey.readOnly
        )

        try {
            stateTransition.verifySignature(newIdentityPublicKey)

            fail("Should throw WrongPublicKeyPurposeError")
        } catch (e: WrongPublicKeyPurposeException) {
            assertEquals(newIdentityPublicKey.purpose, e.publicKeyPurpose)
            assertEquals(IdentityPublicKey.Purpose.AUTHENTICATION, e.keyPurposeRequirement)
        }
    }

    @Test
    fun `#verifySignatureByPublicKeyHash should validate sign by public key hash`() {
        privateKeyHex = "fdfa0d878967ac17ca3e6fa6ca7f647fea51cffac85e41424c6954fcbe97721c"
        val publicKey = "dLfavDCp+ARA3O0AXsOFJ0W//mg="

        stateTransition.signByPrivateKey(privateKeyHex)

        val isValid = stateTransition.verifySignatureByPublicKeyHash(Converters.fromBase64(publicKey))

        assertTrue(isValid)
    }

    @Test
    fun `#verifySignatureByPublicKeyHash should throw an StateTransitionIsNotSignedError error if transition is not signed`() {
        val publicKey = "dLfavDCp+ARA3O0AXsOFJ0W//mg="
        try {
            stateTransition.verifySignatureByPublicKeyHash(Converters.fromBase64(publicKey))

            fail("should throw StateTransitionIsNotSignedError")
        } catch (e: StateTransitionIsNotSignedError) {
            assertEquals(stateTransition, e.stateTransition)
        }
    }

    @Test
    fun `#verifySignatureByPublicKey should validate sign by public key`() {
        privateKeyHex = "9b67f852093bc61cea0eeca38599dbfba0de28574d2ed9b99d10d33dc1bde7b2"
        val publicKey = "A1eUrJ7lM6F1m6dbIyk+vXimKfzki+QRMHMwoAmggt6L"

        stateTransition.signByPrivateKey(privateKeyHex)

        val isValid = stateTransition.verifySignatureByPublicKey(ECKey.fromPublicOnly(Converters.fromBase64(publicKey)))

        assertTrue(isValid)
    }

    @Test
    fun `#verifySignatureByPublicKey should throw an StateTransitionIsNotSignedError error if transition is not signed`() {
        val publicKey = "A1eUrJ7lM6F1m6dbIyk+vXimKfzki+QRMHMwoAmggt6L"
        try {
            stateTransition.verifySignatureByPublicKey(ECKey.fromPublicOnly(Converters.fromBase64(publicKey)))

            fail("should throw StateTransitionIsNotSignedError")
        } catch (e: StateTransitionIsNotSignedError) {
            assertEquals(stateTransition, e.stateTransition)
        }
    }

    @Test fun `#setSignature should set signature`() {
        val signature = "A1eUrA"
        stateTransition.setSignature(signature)

        assertEquals(signature, stateTransition.signature!!.toBase64())
    }

    @Test
    fun `#calculateFee should calculate fee`() {
        val result = stateTransition.calculateFee()
        assertEquals(11, result)
    }
}
