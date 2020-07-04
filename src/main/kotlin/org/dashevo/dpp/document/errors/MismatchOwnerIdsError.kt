package org.dashevo.dpp.document.errors

import org.dashevo.dpp.document.Document
import java.lang.Exception

class MismatchOwnerIdsError(val documents: List<Document>) : Exception("Documents have mixed owner ids")