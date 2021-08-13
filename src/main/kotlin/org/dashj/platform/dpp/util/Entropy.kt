/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashj.platform.dpp.util

import org.bitcoinj.core.Utils
import org.bitcoinj.crypto.LinuxSecureRandom
import java.security.SecureRandom

class Entropy {
    companion object {

        const val SEED_SIZE = 32

        // Init proper random number generator, as some old Android installations have bugs that make it unsecure.
        private var secureRandom: SecureRandom

        init {
            // Init proper random number generator, as some old Android installations have bugs that make it unsecure.
            if (Utils.isAndroidRuntime()) {
                LinuxSecureRandom()
            }
            secureRandom = SecureRandom()
        }

        fun generate(): ByteArray {
            return secureRandom.generateSeed(SEED_SIZE)
        }
    }
}
