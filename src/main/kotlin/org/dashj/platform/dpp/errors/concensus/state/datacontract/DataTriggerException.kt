/**
 * Copyright (c) 2021-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashj.platform.dpp.errors.concensus.state.datacontract

import org.dashj.platform.dpp.errors.concensus.state.StateException
import org.dashj.platform.dpp.identifier.Identifier

abstract class DataTriggerException(
    val dataContractId: Identifier,
    val documentTransitionId: Identifier,
    message: String
) :
    StateException(message) {
    var ownerId: Identifier? = null
}
