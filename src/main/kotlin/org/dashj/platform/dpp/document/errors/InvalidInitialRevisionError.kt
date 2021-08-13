package org.dashj.platform.dpp.document.errors

import org.dashj.platform.dpp.document.Document
import java.lang.Exception

class InvalidInitialRevisionError(val document: Document) :
    Exception("Invalid Document initial revision ${document.revision}")
