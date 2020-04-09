/**
 * Copyright (c) 2018-present, Dash Core Team
 *
 * This source code is licensed under the MIT license found in the
 * COPYING file in the root directory of this source tree.
 */

package org.dashevo.dpp

import com.google.common.io.BaseEncoding
import org.json.JSONObject

fun JSONObject.append(jsonObject: JSONObject): JSONObject {
    jsonObject.keys().forEach {
        this.put(it, jsonObject[it])
    }
    return this
}

/**
 *
 * @receiver ByteArray
 * @return String The HEX or Base16 representation of this
 */
fun ByteArray.toHexString(): String {
    return this.joinToString("") {
        java.lang.String.format("%02x", it)
    }
}

/**
 *
 * @receiver ByteArray
 * @return String The Base64 represenation of this
 */
fun ByteArray.toBase64(): String {
    return BaseEncoding.base64().omitPadding().encode(this)
}

/**
 * @receiver ByteArray
 * @return String The Base64 representation with padding of this
 */
fun ByteArray.toBase64Padded(): String {
    return BaseEncoding.base64().encode(this)
}
