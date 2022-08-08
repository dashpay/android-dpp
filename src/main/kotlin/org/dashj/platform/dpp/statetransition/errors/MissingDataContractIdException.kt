package org.dashj.platform.dpp.statetransition.errors

import org.dashj.platform.dpp.errors.DPPException

class MissingDataContractIdException(rawDocumentTransition: Map<String, Any?>) :
    DPPException("\$dataContractId is not present") {
    val rawDocument = rawDocumentTransition["rawDocument"] as Map<String, Any?>
}
