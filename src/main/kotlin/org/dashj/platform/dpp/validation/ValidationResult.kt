/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashj.platform.dpp.validation

import org.dashj.platform.dpp.errors.concensus.ConcensusException

class ValidationResult(errors: List<ConcensusException>) {
    val errors = arrayListOf<ConcensusException>()
    var data: Any?
    init {
        this.errors.addAll(errors)
        data = null
    }

    fun isValid(): Boolean {
        return errors.isEmpty()
    }

    fun merge(validationResult: ValidationResult) {
        errors.addAll(validationResult.errors)
    }
}
