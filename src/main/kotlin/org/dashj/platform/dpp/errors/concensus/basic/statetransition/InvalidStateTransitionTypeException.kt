/**
 * Copyright (c) 2021-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashj.platform.dpp.errors.concensus.basic.statetransition

import org.dashj.platform.dpp.errors.concensus.ConcensusException

class InvalidStateTransitionTypeException(val type: Int) : ConcensusException("Invalid State Transition type $type") {
    constructor(arguments: List<Any>) : this(arguments[0] as Int) {
        setArguments(arguments)
    }
}
