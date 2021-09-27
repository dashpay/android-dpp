/**
 * Copyright (c) 2021-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashj.platform.dpp.errors.concensus.fee

import org.dashj.platform.dpp.errors.concensus.ConcensusException

class BalanceIsNotEnoughException(val balance: Long, val fee: Long) :
    ConcensusException("Current credits balance $balance is not enough to pay $fee fee") {
    constructor(arguments: List<Any>) : this(
        arguments[0] as Long,
        arguments[1] as Long
    ) {
        setArguments(arguments)
    }
}
