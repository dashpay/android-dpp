/**
 * Copyright (c) 2021-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashj.platform.dpp.errors.concensus.basic.identity

import org.bitcoinj.core.Sha256Hash
import org.dashj.platform.dpp.errors.concensus.ConcensusException

class IdentityAssetLockTransactionOutPointAlreadyExistsException(
    transactionId: Sha256Hash,
    outputIndex: Int
) : ConcensusException("Asset lock transaction $transactionId output $outputIndex already used") {
    constructor(arguments: List<Any>) : this(
        Sha256Hash.wrap(arguments[0] as String),
        arguments[1] as Int
    ) {
        setArguments(arguments)
    }
}
