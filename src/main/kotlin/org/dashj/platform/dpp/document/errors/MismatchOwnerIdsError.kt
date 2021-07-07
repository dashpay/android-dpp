package org.dashj.platform.dpp.document.errors

import java.lang.Exception
import org.dashj.platform.dpp.document.Document

class MismatchOwnerIdsError(val documents: List<Document>) : Exception("Documents have mixed owner ids")
