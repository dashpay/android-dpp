/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashj.platform.dpp.util

import org.bitcoinj.core.Utils
import org.bitcoinj.crypto.LinuxSecureRandom
import org.dashj.platform.dpp.identifier.Identifier
import java.security.SecureRandom

object Entropy {

    private const val SEED_SIZE = 32

    private var mockRandomIdentifier: Identifier? = null
    private var mockEntropy: ByteArray? = null

    // Init proper random number generator, as some old Android installations have bugs that make it unsecure.
    private var secureRandom: SecureRandom

    init {
        // Init proper random number generator, as some old Android installations have bugs that make it unsecure.
        if (Utils.isAndroidRuntime()) {
            LinuxSecureRandom()
        }
        secureRandom = SecureRandom()
    }

    @JvmStatic
    fun generate(): ByteArray {
        if (mockEntropy == null) {
            val bytes32 = ByteArray(32)
            secureRandom.nextBytes(bytes32)
            return bytes32
        }
        return mockEntropy!!
    }

    @JvmStatic
    fun setMockGenerate(mockEntroy: ByteArray?) {
        this.mockEntropy = mockEntroy
    }

    @JvmStatic
    fun generateRandomIdentifier(): Identifier {
        return mockRandomIdentifier ?: Identifier.from(generate())
    }

    fun setRandomIdentifier(mockRandomIdentifier: Identifier?) {
        this.mockRandomIdentifier = mockRandomIdentifier
    }

    @JvmStatic
    fun clearMock() {
        mockEntropy = null
        mockRandomIdentifier = null
    }

    @JvmStatic
    fun generateRandomBytes(size: Int): ByteArray {
        val result = ByteArray(size)
        secureRandom.nextBytes(result)
        return result
    }
}
