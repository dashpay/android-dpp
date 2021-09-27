/**
 * Copyright (c) 2021-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashj.platform.dpp.errors.concensus.document

import org.dashj.platform.dpp.errors.concensus.ConcensusException
import org.dashj.platform.dpp.identifier.Identifier

class DataContractNotPresentException(dataContractId: Identifier) :
    ConcensusException("Data Contract $dataContractId is not present") {
    constructor(arguments: List<Any>) : this(Identifier.from(arguments[0])) {
        setArguments(arguments)
    }
}
