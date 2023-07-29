/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashj.platform.dpp.identity

import org.bitcoinj.core.Sha256Hash
import org.bitcoinj.core.VarInt
import org.dashj.platform.dpp.Metadata
import org.dashj.platform.dpp.ProtocolVersion
import org.dashj.platform.dpp.toBase64
import org.dashj.platform.dpp.util.Cbor
import org.dashj.platform.dpp.util.Entropy.generateRandomIdentifier
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.util.Collections.max

class IdentitySpec {

    lateinit var rawIdentity: Map<String, Any?>
    lateinit var identity: Identity
    private lateinit var metadataFixture: Metadata
    @BeforeEach
    fun beforeEach() {

        rawIdentity = mapOf<String, Any?>(
            "protocolVersion" to ProtocolVersion.latestVersion,
            "id" to generateRandomIdentifier(),
            "publicKeys" to listOf(
                mapOf(
                    "id" to 0,
                    "type" to IdentityPublicKey.Type.ECDSA_SECP256K1.value,
                    "data" to ByteArray(36) { 'a'.toByte() },
                    "purpose" to IdentityPublicKey.Purpose.AUTHENTICATION.value,
                    "securityLevel" to IdentityPublicKey.SecurityLevel.MASTER.value,
                    "readOnly" to false,
                )
            ),
            "balance" to 0L,
            "revision" to 0,
        )

        identity = Identity(rawIdentity)

        metadataFixture = Metadata(42, 0)

        identity.metadata = metadataFixture
    }

    @Test
    fun `constructor should set variables from raw model`() {
        val instance = Identity(rawIdentity)

        assertEquals(instance.id, rawIdentity["id"])
        assertEquals(
            instance.publicKeys,
            (rawIdentity["publicKeys"] as List<Map<String, Any>>).map {
                IdentityPublicKey(it)
            }
        )
    }

    @Test
    fun `getId should return set id`() {
        assertEquals(identity.id, rawIdentity["id"])
    }

    @Test
    fun `getPublicKeys should return set public keys`() {
        assertEquals(
            identity.publicKeys,
            (rawIdentity["publicKeys"] as List<Map<String, Any>>).map {
                IdentityPublicKey(it)
            }
        )
    }

    @Test
    fun `#getPublicKeyById should return a public key for a given id`() {
        val key = identity.getPublicKeyById(0)
        assertEquals(key, IdentityPublicKey((rawIdentity["publicKeys"] as List<Map<String, Any>>)[0]))
    }

    @Test
    fun `#getPublicKeyById should return undefined if there's no key with such id`() {
        val key = identity.getPublicKeyById(3)
        assertEquals(key, null)
    }

    @Test //@Disabled
    fun `toBuffer should return serialized Identity`() {
        val result = identity.toBuffer()

        val identityDataToEncode = identity.toObject().toMutableMap()
        identityDataToEncode.remove("protocolVersion")
        val encoded = Cbor.encode(identityDataToEncode)

        val protocolVersionCompact = VarInt(identity.protocolVersion.toLong())
        val buffer = ByteArray(encoded.size + protocolVersionCompact.sizeInBytes)
        System.arraycopy(protocolVersionCompact.encode(), 0, buffer, 0, protocolVersionCompact.sizeInBytes)
        System.arraycopy(encoded, 0, buffer, protocolVersionCompact.sizeInBytes, encoded.size)

        assertArrayEquals(result, buffer)
    }

    @Test @Disabled
    fun `#hash should return hex string of a buffer return by serialize`() {
        val result = identity.hash()

        val identityDataToEncode = identity.toObject().toMutableMap()
        identityDataToEncode.remove("protocolVersion")
        val encoded = Cbor.encode(identityDataToEncode)

        val protocolVersionCompact = VarInt(identity.protocolVersion.toLong())
        val buffer = ByteArray(encoded.size + protocolVersionCompact.sizeInBytes)
        System.arraycopy(protocolVersionCompact.encode(), 0, buffer, 0, protocolVersionCompact.sizeInBytes)
        System.arraycopy(encoded, 0, buffer, protocolVersionCompact.sizeInBytes, encoded.size)
        assertArrayEquals(result, Sha256Hash.hashTwice(buffer))
    }

    @Test
    fun `toObject should return plain object representation`() {
        assertEquals(identity.toObject(), rawIdentity)
    }

    @Test
    fun `toJSON should return json representation`() {
        val jsonIdentity = identity.toJSON()

        assertEquals(
            jsonIdentity,
            mapOf<String, Any?>(
                "protocolVersion" to ProtocolVersion.latestVersion,
                "id" to rawIdentity["id"].toString(),
                "publicKeys" to listOf(
                    mapOf(
                        "id" to 0,
                        "type" to IdentityPublicKey.Type.ECDSA_SECP256K1.value,
                        "data" to (((rawIdentity["publicKeys"] as List<*>)[0] as Map<*, *>)["data"] as ByteArray)
                            .toBase64(),
                        "purpose" to IdentityPublicKey.Purpose.AUTHENTICATION.value,
                        "securityLevel" to IdentityPublicKey.SecurityLevel.MASTER.value,
                        "readOnly" to false,
                    )
                ),
                "balance" to 0L,
                "revision" to 0,
            )
        )
    }

    @Test
    fun `#getBalance should return set identity balance`() {
        identity.balance = 42
        assertEquals(42, identity.balance)
    }

    @Test
    fun `#setBalance should set identity balance`() {
        identity.balance = 42
        assertEquals(42, identity.balance)
    }

    @Test
    fun `#increaseBalance should increase identity balance`() {
        val result = identity.increaseBalance(42)

        assertEquals(42, result)
        assertEquals(42, identity.balance)
    }

    @Test
    fun `#reduceBalance should reduce identity balance`() {
        identity.balance = 42
        val result = identity.reduceBalance(2)

        assertEquals(40, result)
        assertEquals(40, identity.balance)
    }

    @Test
    fun `#setMetadata should set metadata`() {
        val otherMetadata = Metadata(43, 1)
        identity.metadata = otherMetadata

        assertEquals(identity.metadata, otherMetadata)
    }

    @Test
    fun `#getMetadata should get metadata`() {
        assertEquals(identity.metadata, metadataFixture)
    }

    @Test
    fun `getPublicKeyMaxId should get the biggest public key ID`() {
        identity.publicKeys.addAll(
            listOf(
                IdentityPublicKey(
                    99,
                    IdentityPublicKey.Type.ECDSA_SECP256K1,
                    IdentityPublicKey.Purpose.AUTHENTICATION,
                    IdentityPublicKey.SecurityLevel.MASTER,
                    ByteArray(36) { 'a'.toByte() },
                    false
                ),
                IdentityPublicKey(
                    50,
                    IdentityPublicKey.Type.ECDSA_SECP256K1,
                    IdentityPublicKey.Purpose.AUTHENTICATION,
                    IdentityPublicKey.SecurityLevel.MASTER,
                    ByteArray(36) { 'a'.toByte() },
                    false
                )
            )
        )

        val maxId = identity.getPublicKeyMaxId()

        val publicKeyIds = identity.publicKeys.map { publicKey -> publicKey.id }

        assertEquals(max(publicKeyIds), maxId)
    }
}
