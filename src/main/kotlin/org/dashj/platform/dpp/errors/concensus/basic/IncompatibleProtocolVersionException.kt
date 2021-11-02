/**
 * Copyright (c) 2021-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashj.platform.dpp.errors.concensus.basic

import org.dashj.platform.dpp.errors.concensus.ConcensusException

class IncompatibleProtocolVersionException(
    val parsedProtocolVersion: Int,
    val minimalProtocolVersion: Int
) : ConcensusException(
    """Protocol version $parsedProtocolVersion is not supported.
        Minimal supported protocol version is $minimalProtocolVersion"""
) {
    constructor(arguments: List<Any>) : this(arguments[0] as Int, arguments[1] as Int) {
        setArguments(arguments)
    }
}
