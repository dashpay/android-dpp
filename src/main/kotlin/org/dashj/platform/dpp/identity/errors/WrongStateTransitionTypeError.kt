/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashj.platform.dpp.identity.errors

import org.dashj.platform.dpp.statetransition.StateTransition
import java.lang.Exception

class WrongStateTransitionTypeError(val stateTransition: StateTransition) :
    Exception("Can't apply a state transition to the identity model, wrong state transition type")
