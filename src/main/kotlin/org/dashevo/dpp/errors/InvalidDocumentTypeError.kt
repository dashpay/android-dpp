/**
 * Copyright (c) 2018-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashevo.dpp.errors

import org.dashevo.dpp.contract.Contract

class InvalidDocumentTypeError(contract: Contract, type: String)
    : IllegalStateException("Contract ${contract.name} doesn't contain type $type")
