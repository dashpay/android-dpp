/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashevo.dpp.statetransition.errors

import org.dashevo.dpp.statetransition.StateTransition
import java.lang.Exception

class StateTransitionIsNotSignedError(val stateTransition: StateTransition) : Exception("State transition is not signed")