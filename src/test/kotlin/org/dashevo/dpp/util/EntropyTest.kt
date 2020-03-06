package org.dashevo.dpp.util

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

open class EntropyTest {

    @Test
    fun shouldGenerateRandomString() {
        val firstString = Entropy.generate()
        val secondString = Entropy.generate()

        assertNotEquals(firstString, secondString)
    }

    @Test
    fun shouldValidateEntropy() {
        val string = Entropy.generate()

        assertTrue(Entropy.validate(string))
    }

    @Test
    fun shouldNotValidateInvalidEntropy() {
        val string = "wrong"

        assertFalse(Entropy.validate(string))
    }
}