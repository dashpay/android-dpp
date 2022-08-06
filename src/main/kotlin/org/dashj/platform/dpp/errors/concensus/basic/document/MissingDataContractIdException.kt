package org.dashj.platform.dpp.errors.concensus.basic.document

import org.dashj.platform.dpp.errors.concensus.ConcensusException

class MissingDataContractIdException() : ConcensusException("\$dataContractId is not present") {
    constructor(arguments: List<Any>) : this() {
        setArguments(arguments)
    }
}
