/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashj.platform.dpp.errors

import java.lang.Exception

class InvalidStateTransitionTypeError(val type: Int, val rawStateTransition: Map<String, Any?>) :
    Exception("Invalid State Transition Type: $type")
