/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashevo.dpp.util

import org.bitcoinj.core.Sha256Hash
import java.security.SecureRandom

object Utils {
    private val secureRandom = SecureRandom()

    fun generateRandomId(): String {
        val randomBytes = ByteArray(36)
        secureRandom.nextBytes(randomBytes)
        val randomHash = Sha256Hash.of(randomBytes)
        return randomHash.toStringBase58()
    }

}