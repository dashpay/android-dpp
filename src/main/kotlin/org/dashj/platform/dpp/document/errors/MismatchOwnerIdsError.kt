package org.dashj.platform.dpp.document.errors

import org.dashj.platform.dpp.document.Document
import java.lang.Exception

class MismatchOwnerIdsError(val documents: List<Document>) : Exception("Documents have mixed owner ids")
