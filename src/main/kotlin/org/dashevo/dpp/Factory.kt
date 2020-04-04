package org.dashevo.dpp

abstract class Factory {
    data class Options(val skipValidation: Boolean = false)
}