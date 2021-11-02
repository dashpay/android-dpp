/**
 * Copyright (c) 2021-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashj.platform.dpp.errors.concensus.basic.statetransition

import org.dashj.platform.dpp.errors.concensus.ConcensusException

class MissingStateTransitionTypeException : ConcensusException("State Transition type is not present")
