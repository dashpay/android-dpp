/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashevo.dpp.util

import org.dashevo.dpp.toHexString
import org.json.JSONArray
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CborTest {

    @Test
    fun array2cborTest() {
        val where: List<List<String>> = listOf(listOf("message", "startsWith", "Tutorial"))

        var array = JSONArray(where)
        assertEquals("[[\"message\",\"startsWith\",\"Tutorial\"]]", array.toString())

        var bytes = Cbor.encode(where)
        assertEquals("8183676d6573736167656a73746172747357697468685475746f7269616c", bytes.toHexString())
    }
}