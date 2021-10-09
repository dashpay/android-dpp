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
    fun jsonSchemaErrorTest() {
        val metadata = "Metadata(code=1005,drive-error-data-bin=oWlhcmd1bWVudHOGeD1tdXN0IG1hdGNoIHBhdHRlcm4gIl5bYS" +
            "16QS1aXVthLXpBLVowLTktX117MSw2Mn1bYS16QS1aMC05XSQiZ3BhdHRlcm5qL2RvY3VtZW50c3gsIy9wcm9wZXJ0aWVzL2RvY3V" +
            "tZW50cy9wcm9wZXJ0eU5hbWVzL3BhdHRlcm6hZ3BhdHRlcm54KF5bYS16QS1aXVthLXpBLVowLTktX117MSw2Mn1bYS16QS1aMC05" +
            "XSRnJHNjaGVtYQ)"

        val errorMetadata = ErrorMetadata(metadata)
        assertEquals(Codes.JsonSchemaError, errorMetadata.code)
        assertEquals("arguments", errorMetadata.data.keys.first())

        val exception = ConcensusException.create(errorMetadata.code, errorMetadata.arguments)
        assertEquals(Codes.JsonSchemaError.code, exception.getCode())
        assertEquals("JsonSchemaError", exception.name)
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
