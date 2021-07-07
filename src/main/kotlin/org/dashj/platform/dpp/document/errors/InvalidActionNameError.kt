package org.dashj.platform.dpp.document.errors

import java.lang.Exception

class InvalidActionNameError(val actions: List<String>) : Exception("Invalid document action submitted $actions")
