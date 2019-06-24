/**
 * Copyright (c) 2018-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashevo.dpp

import org.json.JSONObject

fun JSONObject.append(jsonObject: JSONObject): JSONObject {
    jsonObject.keys().forEach {
        this.put(it, jsonObject[it])
    }
    return this
}

fun ByteArray.toHexString(): String {
    return this.joinToString("") {
        java.lang.String.format("%02x", it)
    }
}

