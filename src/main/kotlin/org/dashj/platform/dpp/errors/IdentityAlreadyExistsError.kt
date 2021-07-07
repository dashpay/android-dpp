/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashj.platform.dpp.errors

import java.lang.Exception
import org.dashj.platform.dpp.identity.IdentityCreateTransition

class IdentityAlreadyExistsError(identityCreateTransition: IdentityCreateTransition) :
    Exception("Identity ${identityCreateTransition.identityId} already exists")
