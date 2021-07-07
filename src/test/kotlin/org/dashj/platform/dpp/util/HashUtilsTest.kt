/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashj.platform.dpp.util

import org.bitcoinj.core.Base58
import org.dashj.platform.dpp.toBase58
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class HashUtilsTest {

    @Test
    fun generateDataContractIdTest() {
        val ownerId = Base58.decode("23wdhodag")
        val entropy = Base58.decode("5dz916pTe1")

        assertEquals("CnS7cz4z1qoPsNfEgpgyVnKdtH2u7bgzZXHLcCQt24US", HashUtils.generateDataContractId(ownerId, entropy).toBase58())
    }

    @Test
    fun generateDocumentIdTest() {
        val ownerId = Base58.decode("23wdhodag")
        val entropy = Base58.decode("5dz916pTe1")

        assertEquals("CnS7cz4z1qoPsNfEgpgyVnKdtH2u7bgzZXHLcCQt24US", HashUtils.generateDataContractId(ownerId, entropy).toBase58())
    }
}
