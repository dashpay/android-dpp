/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashevo.dpp.errors

import org.dashevo.dpp.identity.IdentityCreateTransition
import java.lang.Exception

class IdentityAlreadyExistsError(identityCreateTransition: IdentityCreateTransition)
    : Exception("Identity ${identityCreateTransition.identityId} already exists")
