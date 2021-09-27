/**
 * Copyright (c) 2021-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashj.platform.dpp.errors.concensus.state.datacontract

import org.dashj.platform.dpp.identifier.Identifier

class DataTriggerConditionException(dataContractId: Identifier, documentTransitionId: Identifier, message: String) :
    DataTriggerException(dataContractId, documentTransitionId, message) {
    constructor(arguments: List<Any>) : this(
        Identifier.from(arguments[0]),
        Identifier.from(arguments[1]),
        arguments[2] as String
    ) {
        setArguments(arguments)
    }
}
