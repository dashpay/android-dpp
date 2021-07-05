package org.dashj.platform.dpp.util

import org.bitcoinj.core.Coin
import kotlin.math.floor

object CreditsConverter {
    const val RATIO = 1000

    fun convertSatoshiToCredits(amount: Long): Long {
        return amount * RATIO
    }

    fun convertSatoshiToCredits(amount: Coin): Long {
        return amount.value * RATIO
    }

    fun convertCreditsToSatoshis(amount: Long): Long {
        return floor(amount.toDouble() / RATIO).toLong()
    }
}