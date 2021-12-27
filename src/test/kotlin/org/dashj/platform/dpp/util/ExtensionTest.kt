package org.dashj.platform.dpp.util

import org.bitcoinj.core.AddressFormatException
import org.bitcoinj.core.Base58
import org.bitcoinj.core.Sha256Hash
import org.dashj.platform.dpp.assertMapEquals
import org.dashj.platform.dpp.assertMapNotEquals
import org.dashj.platform.dpp.deepCompare
import org.dashj.platform.dpp.deepCopy
import org.dashj.platform.dpp.hashTwice
import org.dashj.platform.dpp.toBase58
import org.dashj.platform.dpp.toBase64
import org.dashj.platform.dpp.toBase64Padded
import org.dashj.platform.dpp.toByteArray
import org.dashj.platform.dpp.toHex
import org.dashj.platform.dpp.toSha256Hash
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.lang.IllegalArgumentException

class ExtensionTest {

    @Test
    fun byteArrayExtensionTests() {
        val base58Expected = "4mZmxva49PBb7BE7srw9o3gixvDfj1dAx1K2dmAAauGp"
        val base58ExpectedTwo = "9rjz23TQ3rA2agxXD56XeDfw63hHJUwuj7joxSBEfRgX"
        val base64Expected = "N/2s61REeUQzP5u8vw5kw19/TwuxJWkzQ1B2/r5IUD0"
        val base64PaddedExpected = "N/2s61REeUQzP5u8vw5kw19/TwuxJWkzQ1B2/r5IUD0="
        val base16Expected = "37fdaceb54447944333f9bbcbf0e64c35f7f4f0bb1256933435076febe48503d"

        val bytesFrom58 = Base58.decode(base58Expected)
        val bytesFrom64 = Converters.fromBase64(base64Expected)
        val bytesFrom16 = Converters.fromHex(base16Expected)
        val bytesFrom58Two = Base58.decode(base58ExpectedTwo)
        val bytesFrom64Padded = Converters.fromBase64Padded(base64PaddedExpected)

        assertArrayEquals(bytesFrom64, bytesFrom58)
        assertArrayEquals(bytesFrom64, bytesFrom16)
        assertArrayEquals(bytesFrom64, bytesFrom64Padded)
        assertNotEquals(bytesFrom58[0], bytesFrom58Two[0])

        // do some round trips

        val base58Actual = bytesFrom16.toBase58()
        val base64Actual = bytesFrom58.toBase64()
        val base64ActualPadded = bytesFrom58.toBase64Padded()
        val base16Actual = bytesFrom64.toHex()
        val base58ActualTwo = bytesFrom58Two.toBase58()

        assertEquals(base58Expected, base58Actual)
        assertEquals(base64Expected, base64Actual)
        assertEquals(base16Expected, base16Actual)
        assertNotEquals(base58Actual, base16Actual)
        assertNotEquals(base58Actual, base64Actual)
        assertNotEquals(base58ActualTwo, base58Actual)
        assertNotEquals(base64Actual, base64ActualPadded)

        val decodedBase58 = base58Actual.toByteArray()
        val decodedBase64 = base64Actual.toByteArray()
        val decodedBase16 = base16Actual.toByteArray()
        val decodedBase58Two = base58ActualTwo.toByteArray()
        val decodedBase64Padded = base64ActualPadded.toByteArray()

        assertArrayEquals(bytesFrom58, decodedBase58)
        assertArrayEquals(bytesFrom64, decodedBase64)
        assertArrayEquals(bytesFrom16, decodedBase16)
        assertArrayEquals(bytesFrom64, decodedBase64Padded)
        assertArrayEquals(decodedBase58Two, bytesFrom58Two)
    }

    @Test
    fun extensionExceptionsTest() {
        val base58ExpectedTwo = "9rjz23TQ3rA2aIgxXD56XeDfw63hHJUwuj7joxSBEfRg"
        val base64Expected = "N/2s61REeUQzP5u8vw5kw19/TwuxJWkzQ1B2/r5IUD?"
        val base64PaddedExpected = "N/2s61REeUQzP5u8vw5kw19/TwuxJWkzQ1B2/r5IUD*"
        val base16Expected = "37fdaceb54447944333f9bbcbf0e64c35f7f4f0bb1256933435076febe48503g"

        assertThrows(IllegalArgumentException::class.java) { Converters.fromBase64(base64Expected) }
        assertThrows(IllegalArgumentException::class.java) { Converters.fromHex(base16Expected) }
        assertThrows(AddressFormatException.InvalidCharacter::class.java) { Base58.decode(base58ExpectedTwo) }
        assertThrows(IllegalArgumentException::class.java) { Converters.fromBase64Padded(base64PaddedExpected) }
    }

    @Test
    fun hashTest() {
        val data = Base58.decode("9rjz23TQ3rA2agxXD56XeDfw63hHJUwuj7joxSBEfRgX")
        val hashTwice = Converters.fromHex("6a7233d717a3fb53a9703bece86896a23791f1d072d578537ca002e3b174f229")

        assertArrayEquals(hashTwice, data.hashTwice())
        assertEquals(hashTwice.toHex(), data.toSha256Hash().toString())
        assertEquals(hashTwice.toHex(), Sha256Hash.wrap(hashTwice).toString())
        assertEquals(hashTwice.toHex(), Sha256Hash.twiceOf(data).toString())
    }

    @Test
    fun deepCompareTest() {
        val mapOne = mapOf(
            "first" to 1,
            "second" to 2.0f,
            "third" to "3",
            "four" to hashMapOf(
                "fifth" to 5,
                "6th" to listOf(6, 60, 600, 6000)
            )
        )
        // hard coded copy
        val mapTwo = mapOf(
            "first" to 1,
            "second" to 2.0f,
            "third" to "3",
            "four" to hashMapOf(
                "fifth" to 5,
                "6th" to listOf(6, 60, 600, 6000)
            )
        )
        // deep copy
        val mapThree = mapTwo.deepCopy()
        // check that a map is equal to itself
        assertMapEquals(mapOne, mapOne)
        // check that a map is equal to a hard coded copy of itself
        assertMapEquals(mapOne, mapTwo)
        // check that a map is equal to a deep of itself
        assertMapEquals(mapOne, mapThree)

        // different value for one key
        val mapFour = mapOf(
            "first" to 1.1,
            "second" to 2.0f,
            "third" to "3",
            "four" to hashMapOf(
                "fifth" to 5,
                "6th" to listOf(6, 60, 600, 6000)
            )
        )
        assertMapNotEquals(mapOne, mapFour)
        // missing a key
        val mapFive = mapOf(
            "first" to 1.1,
            "second" to 2.0f,
            "four" to hashMapOf(
                "fifth" to 5,
                "6th" to listOf(6, 60, 600, 6000)
            )
        )
        assertMapNotEquals(mapOne, mapFive)
        // extra value in the list
        val mapSix = mapOf(
            "first" to 1,
            "second" to 2.0f,
            "third" to "3",
            "four" to hashMapOf(
                "fifth" to 5,
                "6th" to listOf(6, 60, 600, 6000, 60000)
            )
        )
        assertMapNotEquals(mapOne, mapSix)

        // test Lest.deepCompare
        assertTrue(listOf(1, 3, 5, 7, 9).deepCompare(listOf(1, 3, 5, 7, 9)))
        assertFalse(listOf(1, 3, 5, 7, 9).deepCompare(listOf(1, 3, 5, 7, 9, 11)))
        assertFalse(listOf(1, 3, 5, 7, 9).deepCompare(listOf(1, 3, 5, 7, 11)))
    }
}
