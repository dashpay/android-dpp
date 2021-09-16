/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashj.platform.dpp.util

import org.bitcoinj.core.Coin
import kotlin.math.floor

object CreditsConverter {
    private const val RATIO = 1000

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
