package org.dashevo.dpp.document.errors

import org.dashevo.dpp.document.Document
import java.lang.Exception

class InvalidInitialRevisionError(val document: Document) : Exception("Invalid Document initial revision ${document.revision}")