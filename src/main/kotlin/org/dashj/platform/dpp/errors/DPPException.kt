/**
 * Copyright (c) 2021-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashj.platform.dpp.errors

abstract class DPPException(message: String) : Exception(message) {
    val name: String = this::class.java.simpleName.replace("Exception", "Error")
}
