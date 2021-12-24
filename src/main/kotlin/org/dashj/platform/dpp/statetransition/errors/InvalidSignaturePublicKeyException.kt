/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashj.platform.dpp.statetransition.errors

import org.dashj.platform.dpp.errors.DPPException

class InvalidSignaturePublicKeyException(val signaturePublicKey: String) :
    DPPException("Invalid signature public key: $signaturePublicKey")
