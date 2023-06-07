/**
 * Copyright (c) 2021-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashj.platform.dpp.errors

import org.dashj.platform.dpp.errors.concensus.Codes
import org.dashj.platform.dpp.errors.concensus.ConcensusException
import org.dashj.platform.dpp.errors.concensus.basic.identity.InvalidInstantAssetLockProofException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

@Disabled
class ErrorsTest {
    @Test
    fun unauthenticatedTest() {
        val metadata = "Metadata(code=2002,drive-error-data-bin=oWlhcmd1bWVudHOA)"

        val errorMetadata = ConcensusErrorMetadata(metadata)

        assertEquals(Codes.InvalidStateTransitionSignatureError, errorMetadata.code)
        assertEquals("arguments", errorMetadata.data.keys.first())
    }

    @Test
    fun jsonSchemaErrorTest() {
        val metadata = "Metadata(code=1005,drive-error-data-bin=oWlhcmd1bWVudHOGeD1tdXN0IG1hdGNoIHBhdHRlcm4gIl5bYS" +
            "16QS1aXVthLXpBLVowLTktX117MSw2Mn1bYS16QS1aMC05XSQiZ3BhdHRlcm5qL2RvY3VtZW50c3gsIy9wcm9wZXJ0aWVzL2RvY3V" +
            "tZW50cy9wcm9wZXJ0eU5hbWVzL3BhdHRlcm6hZ3BhdHRlcm54KF5bYS16QS1aXVthLXpBLVowLTktX117MSw2Mn1bYS16QS1aMC05" +
            "XSRnJHNjaGVtYQ)"

        val errorMetadata = ConcensusErrorMetadata(metadata)
        assertEquals(Codes.JsonSchemaError, errorMetadata.code)
        assertEquals("arguments", errorMetadata.data.keys.first())

        val exception = ConcensusException.create(errorMetadata.code, errorMetadata.arguments)
        assertEquals(Codes.JsonSchemaError.code, exception.getCode())
        assertEquals("JsonSchemaError", exception.name)
    }

    @Test
    fun createCorrectExceptionClass() {
        val metadata = "Metadata(code=2002,drive-error-data-bin=oWlhcmd1bWVudHOA)"

        val errorMetadata = ConcensusErrorMetadata(metadata)

        val exception = ConcensusException.create(errorMetadata.code, errorMetadata.arguments)

        assertEquals(Codes.InvalidStateTransitionSignatureError.code, exception.getCode())
        assertEquals("InvalidStateTransitionSignatureError", exception.name)
    }

    @Test
    fun noDataCreateCorrectExceptionClass() {
        val metadata = "Metadata(code=2002)"

        val errorMetadata = ConcensusErrorMetadata(metadata)

        val exception = ConcensusException.create(errorMetadata.code, errorMetadata.arguments)

        assertEquals(Codes.InvalidStateTransitionSignatureError.code, exception.getCode())
        assertEquals("InvalidStateTransitionSignatureError", exception.name)
    }

    @Test
    fun invalidInstantAssetLockProofErrorTest() {
        val metadata = "Metadata(code=1041,drive-error-data-bin=oWlhcmd1bWVudHOBeDpJbnZhbGlkIEFyZ3VtZW50OiBFeHBlY3RlZCBzaWduYXR1cmUgdG8gYmUgYSBibHMgc2lnbmF0dXJl)"

        val errorMetadata = ConcensusErrorMetadata(metadata)

        val exception = ConcensusException.create(errorMetadata.code, errorMetadata.arguments)

        assertTrue(exception is InvalidInstantAssetLockProofException)
        assertEquals(Codes.InvalidInstantAssetLockProofError.code, exception.getCode())
        assertEquals("InvalidInstantAssetLockProofError", exception.name)
    }

    @Test
    fun notIndexedPropertiesInWhereConditionsErrorTest() {
        val metaData = "Metadata(drive-error-data-bin=oWZlcnJvcnOBoWRuYW1leCpOb3RJbmRleGVkUHJvcGVydGllc0luV2hlcmVDb25kaXRpb25zRXJyb3I)"

        val errorMetadata = DriveErrorMetadata(metaData)

        println(errorMetadata)

        assertEquals("NotIndexedPropertiesInWhereConditionsError", errorMetadata.getFirstError())
    }

    @Test
    fun invalidPropertiesInOrderByErrorTest() {
        val metaData = "Metadata(drive-error-data-bin=oWZlcnJvcnOBoWRuYW1leB9JbnZhbGlkUHJvcGVydGllc0luT3JkZXJCeUVycm9y)"

        val errorMetadata = DriveErrorMetadata(metaData)

        assertEquals("InvalidPropertiesInOrderByError", errorMetadata.getFirstError())
    }
}
