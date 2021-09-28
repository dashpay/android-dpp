/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashj.platform.dpp.statetransition

import org.bitcoinj.core.ECKey
import org.dashj.platform.dpp.identity.IdentityPublicKey
import org.dashj.platform.dpp.identity.StateTransitionMock
import org.dashj.platform.dpp.statetransition.errors.StateTransitionIsNotSignedError
import org.dashj.platform.dpp.toHex
import org.dashj.platform.dpp.util.Converters
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class StateTransitionIdentitySignedSpec {
    private lateinit var stateTransition: StateTransition
    var protocolVersion: Int = 1
    lateinit var privateKey: ECKey
    private var publicKeyId: Int = 1
    private lateinit var identityPublicKey: IdentityPublicKey

    @BeforeEach
    fun beforeEach() {
        privateKey = ECKey()
        protocolVersion = 1
        publicKeyId = 1
        stateTransition = StateTransitionMock(protocolVersion)

        identityPublicKey = IdentityPublicKey(publicKeyId, IdentityPublicKey.TYPES.ECDSA_SECP256K1, privateKey.pubKey)
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
        val serializedData = stateTransition.toBuffer(skipSignature = true)

        assertEquals("01000000a1647479706500", serializedData.toHex())
    }
    @Test
    fun `#verifySignatureByPublicKey should validate sign by public key`() {
        val privateKey = ECKey.fromPrivate(
            Converters.fromHex("9b67f852093bc61cea0eeca38599dbfba0de28574d2ed9b99d10d33dc1bde7b2")
        )
        val publicKey = ECKey.fromPublicOnly(
            Converters.fromBase64("A1eUrJ7lM6F1m6dbIyk+vXimKfzki+QRMHMwoAmggt6L")
        )

        stateTransition.signByPrivateKey(privateKey)

        assertTrue(stateTransition.verifySignatureByPublicKey(publicKey))
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
}
