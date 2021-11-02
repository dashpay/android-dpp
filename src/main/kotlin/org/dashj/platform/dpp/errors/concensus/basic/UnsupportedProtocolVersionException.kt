/**
 * Copyright (c) 2021-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashj.platform.dpp.errors.concensus.basic

import org.dashj.platform.dpp.errors.concensus.ConcensusException

class UnsupportedProtocolVersionException(
    val parsedProtocolVersion: Int,
    val latestVersion: Int
) : ConcensusException(
    """Protocol version $parsedProtocolVersion is not supported. 
        Latest supported version is $latestVersion"""
) {
    constructor(arguments: List<Any>) : this(arguments[0] as Int, arguments[1] as Int) {
        setArguments(arguments)
    }
}
