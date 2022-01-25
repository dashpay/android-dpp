/**
 * Copyright (c) 2022-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashj.platform.dpp.errors

abstract class ErrorMetadata(val metadata: String) {

    override fun toString(): String {
        return metadata
    }
}
