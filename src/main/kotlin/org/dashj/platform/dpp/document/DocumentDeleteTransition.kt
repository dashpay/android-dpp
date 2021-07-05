/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashevo.dpp.document

class DocumentDeleteTransition : DocumentTransition {

    override val action = Action.DELETE

    constructor(rawStateTransition: MutableMap<String, Any?>) : super(rawStateTransition)

}