package org.dashj.platform.dpp.errors.concensus

class UnknownConcensusError(code: Int, driveErrorData: List<Any>) : ConcensusException("Unknown error $code")
