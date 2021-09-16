package org.dashj.platform.dpp.util

import org.bitcoinj.core.Base58
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test

class ConverterTest {
    @Test
    fun byteArrayTests() {
        val base58Expected = "4mZmxva49PBb7BE7srw9o3gixvDfj1dAx1K2dmAAauGp"
        val base64Expected = "N/2s61REeUQzP5u8vw5kw19/TwuxJWkzQ1B2/r5IUD0"

        val bytesFrom58 = Base58.decode(base58Expected)
        val bytesFrom64 = Converters.fromBase64(base64Expected)

        val b1 = Converters.byteArrayFromBase64orByteArray(base64Expected)
        val b2 = Converters.byteArrayFromBase64orByteArray(bytesFrom64)

        assertArrayEquals(b1, b2)

        val b3 = Converters.byteArrayFromBase58orByteArray(base58Expected)
        val b4 = Converters.byteArrayFromBase58orByteArray(bytesFrom58)

        assertArrayEquals(b3, b4)
        assertArrayEquals(b3, b2)
    }
}
