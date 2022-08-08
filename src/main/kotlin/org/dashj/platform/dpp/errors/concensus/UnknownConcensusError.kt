package org.dashj.platform.dpp.errors.concensus

class UnknownConcensusError(code: Int, val driveErrorData: List<Any>) : ConcensusException("Unknown error $code")
