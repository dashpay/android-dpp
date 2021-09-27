package org.dashj.platform.dpp.errors.concensus.state.document

import org.dashj.platform.dpp.errors.concensus.state.StateException
import org.dashj.platform.dpp.identifier.Identifier

class InvalidDocumentRevisionException(val documentId: Identifier, val currentRevision: Int) :
    StateException("Document $documentId has invalid revision. The current revision is $currentRevision") {
    constructor(arguments: List<Any>) : this(Identifier.from(arguments[0]), arguments[1] as Int) {
        setArguments(arguments)
    }
}
