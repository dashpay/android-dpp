package org.dashevo.dpp.util

import org.bitcoinj.core.Coin
import org.dashevo.dpp.util.CreditsConverter.convertCreditsToSatoshis
import org.dashevo.dpp.util.CreditsConverter.convertSatoshiToCredits
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CreditsConverterTest {

    @Test
    fun creditsConverterTest() {
        assertEquals(225000L, convertSatoshiToCredits(225))
        assertEquals(225000L, convertSatoshiToCredits(Coin.valueOf(225)))
        assertEquals(225, convertCreditsToSatoshis(225000))
    }
}