/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashj.platform.dpp.statetransition.errors

import java.lang.Exception

class InvalidSignaturePublicKeyError(val signaturePublicKey: String) : Exception("Invalid signature public key")