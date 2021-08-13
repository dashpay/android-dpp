/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashj.platform.dpp

data class Metadata(val blockHeight: Int, val coreChainLockedHeight: Int) {
    constructor(rawMetaData: Map<String, Any?>) :
        this(rawMetaData["blockHeight"].toString().toInt(), rawMetaData["coreChainLockedHeight"].toString().toInt())
}
