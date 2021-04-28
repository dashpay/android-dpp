package org.dashevo.dpp.errors

open class ConcensusException (message: String) : Exception (message) {
    val name: String = this::class.simpleName!!
}