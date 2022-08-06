/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashj.platform.dpp.document

import org.dashj.platform.dpp.contract.DataContract

class DocumentDeleteTransition(rawStateTransition: MutableMap<String, Any?>, dataContract: DataContract) :
    DocumentTransition(rawStateTransition, dataContract) {

    override val action = Action.DELETE
}
