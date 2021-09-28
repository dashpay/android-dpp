/**
 * Copyright (c) 2021-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashj.platform.dpp.errors

import org.dashj.platform.dpp.errors.concensus.Codes
import org.dashj.platform.dpp.errors.concensus.ConcensusException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ErrorsTest {
    @Test
    fun unauthenticatedTest() {
        val metadata = "Metadata(code=2002,drive-error-data-bin=oWlhcmd1bWVudHOA)"

        val errorMetadata = ErrorMetadata(metadata)

        assertEquals(Codes.InvalidStateTransitionSignatureError, errorMetadata.code)
        assertEquals("arguments", errorMetadata.data.keys.first())
    }

    @Test
    fun createCorrectExceptionClass() {
        val metadata = "Metadata(code=2002,drive-error-data-bin=oWlhcmd1bWVudHOA)"

        val errorMetadata = ErrorMetadata(metadata)

        val exception = ConcensusException.create(errorMetadata.code, errorMetadata.arguments)

        assertEquals(Codes.InvalidStateTransitionSignatureError.code, exception.getCode())
        assertEquals("InvalidStateTransitionSignatureError", exception.name)
    }
}
