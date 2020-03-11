/**
 * Copyright (c) 2018-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashevo.dpp.identity

import org.dashevo.dpp.Factory
import org.dashevo.dpp.util.HashUtils

class IdentityFactory() : Factory() {

    fun create(id: String, type: Identity.IdentityType, publicKeys: List<IdentityPublicKey>) : Identity {
        return Identity(id, type, publicKeys)
    }

    fun createFromObject(rawIdentity: MutableMap<String, Any>, options: Options = Options()): Identity {
        return Identity(rawIdentity)
    }

    fun createFromSerialized(payload: ByteArray, options: Options = Options()): Identity {
        val rawIdentity = HashUtils.decode(payload).toMutableMap()
        return createFromObject(rawIdentity, options)
    }
}