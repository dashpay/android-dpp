/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashevo.dpp.errors

import java.lang.Exception

class InvalidStateTransitionTypeError(val type: Int, rawStateTransition: Map<String, Any>) : Exception("Invalid State Transition Type: $type")