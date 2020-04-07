/**
 * Copyright (c) 2020-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */
package org.dashevo.dpp.util

import org.bitcoinj.core.Address
import org.bitcoinj.core.AddressFormatException
import org.bitcoinj.core.ECKey
import org.bitcoinj.params.TestNet3Params

class Entropy {
    companion object {

        fun generate() : String {
            val privateKey = ECKey()
            return Address.fromKey(TestNet3Params.get(), privateKey).toString()
        }

        fun generateBytes() : ByteArray {
            return ECKey().pubKeyHash
        }

        fun validate(address : String) : Boolean {
            return try {
                Address.fromBase58(TestNet3Params.get(), address)
                true
            } catch (_ : AddressFormatException) {
                false
            }
        }
    }
}