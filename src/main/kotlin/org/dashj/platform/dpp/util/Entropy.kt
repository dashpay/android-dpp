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
    private var mockEntroy: ByteArray? = null

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
        return mockEntroy ?: secureRandom.generateSeed(SEED_SIZE)
    }

    @JvmStatic
    fun setMockGenerate(mockEntroy: ByteArray?) {
        this.mockEntroy = mockEntroy
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
        mockEntroy = null
        mockRandomIdentifier = null
    }
}
