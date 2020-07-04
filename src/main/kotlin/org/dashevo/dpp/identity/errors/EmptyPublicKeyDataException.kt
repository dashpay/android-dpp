/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashevo.dpp.identity.errors

import org.dashevo.dpp.statetransition.StateTransition
import java.lang.Exception

class EmptyPublicKeyDataException() : Exception("Public key data is not set")