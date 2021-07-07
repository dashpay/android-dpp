package org.dashj.platform.dpp.document.errors

import java.lang.Exception
import org.dashj.platform.dpp.document.Document

class InvalidInitialRevisionError(val document: Document) : Exception("Invalid Document initial revision ${document.revision}")
