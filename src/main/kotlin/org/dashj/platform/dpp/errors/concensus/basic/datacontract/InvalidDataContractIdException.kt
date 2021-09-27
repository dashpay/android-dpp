/**
 * Copyright (c) 2021-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashj.platform.dpp.errors.concensus.basic.datacontract

import org.dashj.platform.dpp.errors.concensus.ConcensusException
import org.dashj.platform.dpp.identifier.Identifier

class InvalidDataContractIdException(val expectedId: Identifier, val invalidId: Identifier) :
    ConcensusException("Data Contract ID must be $expectedId, got $invalidId") {
    constructor(arguments: List<Any>) : this(
        Identifier.from(arguments[0]),
        Identifier.from(arguments[1])
    ) {
        setArguments(arguments)
    }
}
