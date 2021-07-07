/**
 * Copyright (c) 2018-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashj.platform.dpp.errors

import org.dashj.platform.dpp.contract.DataContract

class InvalidDocumentTypeError(dataContract: DataContract, type: String) :
    IllegalStateException("Contract ${dataContract.id} doesn't contain type $type")
