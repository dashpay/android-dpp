/**
 * Copyright (c) 2021-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashj.platform.dpp.identity

import org.dashj.platform.dpp.identity.errors.EmptyPublicKeyDataException
import org.dashj.platform.dpp.util.Converters
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class IdentityPublicKeySpec {
    lateinit var rawPublicKey: Map<String, Any?>
    lateinit var publicKey: IdentityPublicKey

    @BeforeEach
    fun beforeEach() {
        rawPublicKey = mapOf<String, Any>(
            "id" to 0,
            "type" to IdentityPublicKey.Type.ECDSA_SECP256K1,
            "data" to Converters.byteArrayFromBase64orByteArray("AkVuTKyF3YgKLAQlLEtaUL2HTditwGILfWUVqjzYnIgH"),
            "purpose" to IdentityPublicKey.Purpose.AUTHENTICATION,
            "securityLevel" to IdentityPublicKey.SecurityLevel.MASTER,
            "readOnly" to false,
        )

        publicKey = IdentityPublicKey(rawPublicKey as Map<String, Any>)
    }

    @Test
    fun `#getId should return set id`() {
        assertEquals(publicKey.id, rawPublicKey["id"] as Int)
    }

    @Test
    fun `#getData should return set data`() {
        assertEquals(publicKey.data, rawPublicKey["data"])
    }

    @Test
    fun `#getPurpose should return set data`() {
        assertEquals(publicKey.purpose, rawPublicKey["purpose"])
    }

    @Test
    fun `#getSecurityLevel should return set data`() {
        assertEquals(publicKey.securityLevel, rawPublicKey["securityLevel"])
    }

    @Test
    fun `#getReadOnly should return readOnly`() {
        assertEquals(publicKey.readOnly, rawPublicKey["readOnly"])
    }

    @Test
    fun `#hash should return original public key hash`() {
        val result = publicKey.hash()
        val expectedHash = Converters.fromBase64("Q/5mfilFPdZt+Fr5JWC1+tg0cPs=")
        assertArrayEquals(expectedHash, result)
    }

    @Test
    fun `#hash should return data in case ECDSA_HASH160`() {
        rawPublicKey = mapOf(
            "id" to 0,
            "type" to IdentityPublicKey.Type.ECDSA_HASH160,
            "data" to Converters.fromBase64("AkVuTKyF3YgKLAQlLEtaUL2HTditwGILfWUVqjzYnIgH"),
            "purpose" to IdentityPublicKey.Purpose.AUTHENTICATION,
            "securityLevel" to IdentityPublicKey.SecurityLevel.MASTER
        )

        publicKey = IdentityPublicKey(rawPublicKey as Map<String, Any>)

        val result = publicKey.hash()

        val expectedHash = Converters.fromBase64("AkVuTKyF3YgKLAQlLEtaUL2HTditwGILfWUVqjzYnIgH")

        assertArrayEquals(expectedHash, result)
    }

    @Test
    fun `#hash should throw invalid argument error if data was not originally provided`() {
        publicKey = IdentityPublicKey(
            mapOf(
                "id" to 0,
                "type" to IdentityPublicKey.Type.ECDSA_SECP256K1,
            )
        )

        try {
            publicKey.hash()
            fail("Exception was not thrown")
        } catch (e: EmptyPublicKeyDataException) {
            assertEquals("Public key data is not set", e.message)
        } catch (e: Exception) {
            fail("Incorrect exception thrown")
        }
    }

    @Test
    fun `#toJSON should return JSON representation`() {
        val jsonPublicKey = publicKey.toJSON()

        assertEquals(
            jsonPublicKey,
            mapOf(
                "id" to 0,
                "type" to IdentityPublicKey.Type.ECDSA_SECP256K1.value,
                "data" to "AkVuTKyF3YgKLAQlLEtaUL2HTditwGILfWUVqjzYnIgH",
                "purpose" to IdentityPublicKey.Purpose.AUTHENTICATION.value,
                "securityLevel" to IdentityPublicKey.SecurityLevel.MASTER.value,
                "readOnly" to false
            )
        )
    }

    @Test
    fun `isMaster should return true when public key has MASTER security level`() {
        val newIdentityPublicKey = IdentityPublicKey(
            publicKey.id,
            publicKey.type,
            publicKey.purpose,
            IdentityPublicKey.SecurityLevel.MASTER,
            publicKey.data,
            publicKey.readOnly
        )

        assertTrue(newIdentityPublicKey.isMaster())
    }

    @Test
    fun `isMaster should return false when public key doesn't have MASTER security level`() {
        val newIdentityPublicKey = IdentityPublicKey(
            publicKey.id,
            publicKey.type,
            publicKey.purpose,
            IdentityPublicKey.SecurityLevel.HIGH,
            publicKey.data,
            publicKey.readOnly
        )

        assertFalse(newIdentityPublicKey.isMaster())
    }
}
