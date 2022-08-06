package org.dashj.platform.dpp.identifier

import org.bitcoinj.core.Base58
import org.dashj.platform.dpp.identifier.errors.IdentifierException
import org.dashj.platform.dpp.toBase64
import org.dashj.platform.dpp.util.Entropy
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class IdentifierSpec {
    lateinit var buffer: ByteArray

    @BeforeEach
    fun beforeEach() {
        buffer = Entropy.generateRandomBytes(32)
    }

    @Test
    fun `#constructor should accept Buffer`() {
        val identifier = Identifier(buffer)
        assertEquals(identifier.toBuffer(), buffer)
    }

    @Test
    fun `#constructor should throw error if buffer is not 32 bytes long`() {
        assertThrows<IdentifierException> {
            Identifier(ByteArray(30))
        }
    }

    @Test
    fun `#toBuffer should return a normal Buffer`() {
        val identifier = Identifier(buffer)
        assertArrayEquals(identifier.toBuffer(), buffer)
    }

    @Test
    fun `#encodeCBOR should encode using cbor encoder`() {
        val identifier = Identifier(buffer)
        val result = identifier.encodeCBOR()
        assertNotNull(result)
    }

    @Test
    fun `#toJSON should return a base58 encoded string`() {
        val identifier = Identifier(buffer)
        val string = identifier.toJSON()
        assertEquals(string, Base58.encode(buffer))
    }

    @Test
    fun `toString should return a base58 encoded string by default`() {
        val base58string = Base58.encode(buffer)

        val identifier = Identifier(buffer)

        val string = identifier.toString()

        assertEquals(string, base58string)
    }

    @Test
    fun `#toString should return a string encoded with specified encoding`() {
        val identifier = Identifier(buffer)

        val string = identifier.toString("base64")

        assertEquals(string, buffer.toBase64())
    }

    @Test
    fun `from should create an instance from Buffer`() {
        val identifier = Identifier.from(buffer)
        assertArrayEquals(identifier.toBuffer(), buffer)
    }

    @Test
    fun `from should create an instance with a base58 string`() {
        val string = Base58.encode(buffer)
        val identifier = Identifier.from(string)
        assertArrayEquals(identifier.toBuffer(), buffer)
    }

    @Test
    fun `from should create an instance with a base64 string`() {
        val string = buffer.toBase64()
        val identifier = Identifier.from(string, "base64")
        assertArrayEquals(identifier.toBuffer(), buffer)
    }
}
