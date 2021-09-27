/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashj.platform.dpp.errors.concensus.state.identity

import org.dashj.platform.dpp.errors.concensus.state.StateException
import org.dashj.platform.dpp.identifier.Identifier

class IdentityAlreadyExistsException(val identityId: Identifier) :
    StateException("Identity $identityId already exists") {
    constructor(arguments: List<Any>) : this(Identifier.from(arguments[0])) {
        setArguments(arguments)
    }
}
